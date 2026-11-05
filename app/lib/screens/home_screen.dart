import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/alert_item.dart';
import '../services/api_service.dart';
import '../services/notification_service.dart';
import '../services/websocket_service.dart';
import '../theme/app_theme.dart';
import '../widgets/status_banner.dart';
import '../widgets/baby_status_card.dart';
import '../widgets/theft_status_card.dart';
import '../widgets/alert_card.dart';
import '../widgets/alert_detail_dialog.dart';
import 'register_face_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final WebSocketService _wsService = WebSocketService();
  final List<AlertItem> _alerts = [];
  bool _isLoading = true;
  bool _isConnected = false;

  String _babyStatus = 'TRE DANG THUC';
  String _babyColor = 'GREEN';
  String _securityStatus = 'AN TOAN';
  String _securityColor = 'GREEN';
  String _activeFilter = 'ALL';

  String _serverIp = '192.168.100.225:8080';

  @override
  void initState() {
    super.initState();
    try {
      final host = Uri.base.host;
      if (host.isNotEmpty && host != '0.0.0.0') {
        _serverIp = '$host:8080';
      }
    } catch (_) {}
    _initConnection();
    _loadInitialData();
  }

  String _cleanHost(String input) {
    var s = input.trim();
    s = s.replaceAll(RegExp(r'^(http:\/\/|https:\/\/|ws:\/\/|wss:\/\/)', caseSensitive: false), '');
    s = s.replaceAll(RegExp(r'\/.*$'), '');
    if (s.isEmpty) {
      s = 'localhost:8080';
    } else if (!s.contains(':')) {
      s = '$s:8080';
    }
    return s;
  }

  void _initConnection() {
    final host = _cleanHost(_serverIp);
    ApiService.setBaseUrl('http://$host');

    _wsService.onConnectionChange = (connected) {
      if (mounted) setState(() => _isConnected = connected);
    };

    _wsService.onNewAlert = (newAlert) {
      if (mounted) {
        setState(() => _alerts.insert(0, newAlert));

        // Push notification on Android
        if (!kIsWeb) {
          NotificationService.showAlertNotification(
            id: newAlert.id,
            title: newAlert.title,
            body: newAlert.message,
            isCritical: newAlert.isCritical,
            alertType: newAlert.alertType,
          );
        }

        try {
          HapticFeedback.heavyImpact();
          HapticFeedback.vibrate();
        } catch (_) {}

        if (newAlert.isCritical) {
          showDialog(
            context: context,
            barrierDismissible: true,
            builder: (ctx) => AlertDetailDialog(
              alert: newAlert,
              onResolve: () => _resolveAlert(newAlert),
              onDelete: () {
                Navigator.pop(ctx);
                _deleteAlert(newAlert);
              },
            ),
          );
        }

        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: newAlert.isCritical ? AppTheme.red : AppTheme.amber,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
            content: Row(
              children: [
                const Icon(Icons.crisis_alert_rounded, color: Colors.white),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(newAlert.title,
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                      Text(newAlert.message,
                          style: const TextStyle(fontSize: 12, color: Colors.white70),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                    ],
                  ),
                ),
              ],
            ),
            duration: const Duration(seconds: 5),
          ),
        );
      }
    };

    _wsService.onStatusUpdate = (statusMap) {
      if (mounted) {
        setState(() {
          _babyStatus = statusMap['baby_status'] ?? _babyStatus;
          _babyColor = statusMap['baby_status_color'] ?? _babyColor;
          _securityStatus = statusMap['security_status'] ?? _securityStatus;
          _securityColor = statusMap['security_status_color'] ?? _securityColor;
        });
      }
    };

    _wsService.onAlertResolved = (alertId) {
      if (mounted) {
        setState(() {
          final idx = _alerts.indexWhere((a) => a.id == alertId);
          if (idx != -1) _alerts[idx].isResolved = true;
        });
      }
    };

    _wsService.onAlertDeleted = (alertId) {
      if (mounted) {
        setState(() => _alerts.removeWhere((a) => a.id == alertId));
      }
    };

    _wsService.onAlertsCleared = () {
      if (mounted) {
        setState(() => _alerts.clear());
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: AppTheme.bg3,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            content: const Row(
              children: [
                Icon(Icons.delete_sweep_rounded, color: Colors.white70),
                SizedBox(width: 10),
                Text('Đã xóa tất cả thông báo', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
              ],
            ),
          ),
        );
      }
    };

    _wsService.connect('ws://$host/ws');
  }

  Future<void> _loadInitialData() async {
    setState(() => _isLoading = true);
    final alerts = await ApiService.fetchAlerts();
    final status = await ApiService.fetchStatus();

    if (mounted) {
      setState(() {
        _alerts.clear();
        _alerts.addAll(alerts);
        if (status.isNotEmpty) {
          _babyStatus = status['baby_status'] ?? _babyStatus;
          _babyColor = status['baby_status_color'] ?? _babyColor;
          _securityStatus = status['security_status'] ?? _securityStatus;
          _securityColor = status['security_status_color'] ?? _securityColor;
        }
        _isLoading = false;
      });
    }
  }

  Future<void> _resolveAlert(AlertItem alert) async {
    final ok = await ApiService.markAlertAsResolved(alert.id);
    if (ok && mounted) {
      setState(() => alert.isResolved = true);
    }
  }

  Future<void> _deleteAlert(AlertItem alert) async {
    final ok = await ApiService.deleteAlert(alert.id);
    if (ok && mounted) {
      setState(() => _alerts.removeWhere((a) => a.id == alert.id));
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Không thể xóa thông báo. Vui lòng thử lại.'),
          backgroundColor: AppTheme.red,
        ),
      );
    }
  }

  Future<void> _confirmClearAllAlerts() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.bg2,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppTheme.cardRadius),
          side: BorderSide(color: Colors.white.withOpacity(0.08)),
        ),
        title: const Row(
          children: [
            Icon(Icons.delete_sweep_rounded, color: AppTheme.red, size: 24),
            SizedBox(width: 10),
            Text('Xóa tất cả thông báo?', style: TextStyle(color: AppTheme.textPrimary, fontWeight: FontWeight.bold, fontSize: 17)),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppTheme.red.withOpacity(0.1),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppTheme.red.withOpacity(0.2)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.warning_amber_rounded, color: AppTheme.amber, size: 24),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Thao tác này sẽ xóa vĩnh viễn tất cả lịch sử cảnh báo khỏi cơ sở dữ liệu. Không thể hoàn tác!',
                      style: TextStyle(color: AppTheme.textSecondary, fontSize: 13, height: 1.4),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actionsPadding: const EdgeInsets.fromLTRB(18, 0, 18, 18),
        actions: [
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.red,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              icon: const Icon(Icons.delete_forever_rounded, size: 20),
              label: const Text('Xác nhận xóa tất cả', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              onPressed: () => Navigator.pop(ctx, true),
            ),
          ),
          const SizedBox(height: 8),
          SizedBox(
            width: double.infinity,
            child: TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Hủy bỏ', style: TextStyle(color: AppTheme.textSecondary)),
            ),
          ),
        ],
      ),
    );

    if (confirm == true && mounted) {
      final ok = await ApiService.clearAllAlerts();
      if (ok && mounted) {
        setState(() => _alerts.clear());
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: AppTheme.bg3,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            content: const Row(
              children: [
                Icon(Icons.check_circle_rounded, color: AppTheme.green),
                SizedBox(width: 10),
                Text('Đã xóa toàn bộ lịch sử cảnh báo', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
              ],
            ),
          ),
        );
      }
    }
  }

  void _showServerSettings() {
    final controller = TextEditingController(text: _serverIp);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.bg2,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppTheme.cardRadius),
          side: BorderSide(color: Colors.white.withOpacity(0.08)),
        ),
        title: const Row(
          children: [
            Icon(Icons.settings_ethernet_rounded, color: AppTheme.blue, size: 24),
            SizedBox(width: 10),
            Text('Cấu hình IP máy chủ', style: TextStyle(color: AppTheme.textPrimary, fontSize: 17, fontWeight: FontWeight.bold)),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Nhập địa chỉ IP & Port của máy chủ Dart backend:',
              style: TextStyle(color: AppTheme.textSecondary, fontSize: 13),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: controller,
              style: const TextStyle(color: AppTheme.textPrimary, fontWeight: FontWeight.w600),
              decoration: InputDecoration(
                filled: true,
                fillColor: AppTheme.bg0,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: AppTheme.blue, width: 1.5),
                ),
                hintText: 'ví dụ: 192.168.100.225:8080',
                hintStyle: TextStyle(color: Colors.white.withOpacity(0.3)),
                prefixIcon: const Icon(Icons.wifi_rounded, color: AppTheme.blue),
              ),
            ),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppTheme.blue.withOpacity(0.08),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: AppTheme.blue.withOpacity(0.2)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.info_outline_rounded, color: AppTheme.blue, size: 18),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Máy chủ và điện thoại phải kết nối cùng mạng WiFi.',
                      style: TextStyle(color: AppTheme.textSecondary, fontSize: 12),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Hủy', style: TextStyle(color: AppTheme.textSecondary)),
          ),
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.blue,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 11),
            ),
            icon: const Icon(Icons.link_rounded, size: 18),
            label: const Text('Kết nối', style: TextStyle(fontWeight: FontWeight.bold)),
            onPressed: () {
              final newIp = _cleanHost(controller.text.trim());
              if (newIp.isNotEmpty) {
                setState(() => _serverIp = newIp);
                _initConnection();
                _loadInitialData();
              }
              Navigator.pop(ctx);
            },
          ),
        ],
      ),
    );
  }

  bool get _hasActiveCriticalAlert => _alerts.any((a) => a.isCritical && !a.isResolved);
  AlertItem? get _activeCriticalAlert {
    try {
      return _alerts.firstWhere((a) => a.isCritical && !a.isResolved);
    } catch (_) {
      return null;
    }
  }

  List<AlertItem> get _filteredAlerts {
    if (_activeFilter == 'INTRUDER') return _alerts.where((a) => a.isIntruder).toList();
    if (_activeFilter == 'BABY') return _alerts.where((a) => a.isBabyDanger || a.isBabyWake).toList();
    if (_activeFilter == 'UNRESOLVED') return _alerts.where((a) => !a.isResolved).toList();
    return _alerts;
  }

  @override
  void dispose() {
    _wsService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.bg1,
      appBar: AppBar(
        backgroundColor: AppTheme.bg0,
        elevation: 0,
        titleSpacing: 16,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(
            color: Colors.white.withOpacity(0.06),
            height: 1,
          ),
        ),
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                gradient: AppTheme.blueGradient,
                borderRadius: BorderRadius.circular(12),
                boxShadow: [
                  BoxShadow(
                    color: AppTheme.blue.withOpacity(0.3),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: const Icon(Icons.shield_rounded, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 12),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Baby & SafeHome AI',
                  style: TextStyle(
                    color: AppTheme.textPrimary,
                    fontWeight: FontWeight.w800,
                    fontSize: 16,
                    letterSpacing: 0.3,
                  ),
                ),
                Text(
                  'Trông Trẻ & An Ninh Gia Đình',
                  style: TextStyle(
                    color: AppTheme.textSecondary,
                    fontSize: 11,
                    letterSpacing: 0.2,
                  ),
                ),
              ],
            ),
          ],
        ),
        actions: [
          // Online/Offline Pill
          GestureDetector(
            onTap: _showServerSettings,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              margin: const EdgeInsets.only(right: 6),
              decoration: BoxDecoration(
                color: _isConnected
                    ? AppTheme.green.withOpacity(0.14)
                    : AppTheme.red.withOpacity(0.14),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: _isConnected
                      ? AppTheme.green.withOpacity(0.4)
                      : AppTheme.red.withOpacity(0.4),
                ),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 7,
                    height: 7,
                    decoration: BoxDecoration(
                      color: _isConnected ? AppTheme.green : AppTheme.red,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 6),
                  Text(
                    _isConnected ? 'Online' : 'Offline',
                    style: TextStyle(
                      color: _isConnected ? AppTheme.green : AppTheme.red,
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.settings_outlined, color: AppTheme.textSecondary, size: 22),
            tooltip: 'Cài đặt IP server',
            onPressed: _showServerSettings,
          ),
          IconButton(
            icon: const Icon(Icons.face_retouching_natural, color: AppTheme.textSecondary, size: 22),
            tooltip: 'Đăng ký khuôn mặt',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const RegisterFaceScreen()),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.refresh_rounded, color: AppTheme.textSecondary, size: 22),
            tooltip: 'Tải lại',
            onPressed: _loadInitialData,
          ),
          const SizedBox(width: 4),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadInitialData,
        color: AppTheme.blue,
        backgroundColor: AppTheme.bg2,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Emergency or Safe Status Banner
              StatusBanner(
                isAlarmActive: _hasActiveCriticalAlert,
                alarmTitle: _activeCriticalAlert?.title ?? 'Phát hiện sự cố khẩn cấp!',
                onDismissAlarm: () {
                  if (_activeCriticalAlert != null) _resolveAlert(_activeCriticalAlert!);
                },
              ),

              // Status Cards
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                child: Column(
                  children: [
                    TheftStatusCard(status: _securityStatus, statusColor: _securityColor),
                    const SizedBox(height: 12),
                    BabyStatusCard(status: _babyStatus, statusColor: _babyColor),
                  ],
                ),
              ),

              // Simulation Quick Test Bar
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppTheme.bg2,
                  borderRadius: BorderRadius.circular(AppTheme.cardRadius),
                  border: Border.all(color: Colors.white.withOpacity(0.06)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(6),
                          decoration: BoxDecoration(
                            gradient: AppTheme.amberGradient,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Icon(Icons.bolt_rounded, color: Colors.white, size: 16),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'Mô Phỏng Tình Huống Cảnh Báo',
                          style: TextStyle(
                            color: AppTheme.textPrimary,
                            fontWeight: FontWeight.bold,
                            fontSize: 13.5,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    Row(
                      children: [
                        Expanded(
                          child: _demoButton(
                            'Kẻ Lạ Mặt',
                            Icons.person_off_rounded,
                            AppTheme.red,
                            () => ApiService.triggerDemoAlert('intruder'),
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: _demoButton(
                            'Bé Nguy Cơ',
                            Icons.child_care_rounded,
                            AppTheme.amber,
                            () => ApiService.triggerDemoAlert('baby_danger'),
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: _demoButton(
                            'Khói / Lửa',
                            Icons.local_fire_department_rounded,
                            AppTheme.orange,
                            () => ApiService.triggerDemoAlert('fire_smoke'),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              // Alerts Header & Filter Chips
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 2),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Text(
                          'Lịch Sử Cảnh Báo',
                          style: TextStyle(
                            color: AppTheme.textPrimary,
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.3,
                          ),
                        ),
                        const SizedBox(width: 10),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3),
                          decoration: BoxDecoration(
                            color: AppTheme.blue.withOpacity(0.18),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: AppTheme.blue.withOpacity(0.3)),
                          ),
                          child: Text(
                            '${_filteredAlerts.length}',
                            style: const TextStyle(
                              color: AppTheme.blue,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    if (_alerts.isNotEmpty)
                      Material(
                        color: Colors.transparent,
                        child: InkWell(
                          borderRadius: BorderRadius.circular(10),
                          onTap: _confirmClearAllAlerts,
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                            decoration: BoxDecoration(
                              color: AppTheme.red.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(color: AppTheme.red.withOpacity(0.3)),
                            ),
                            child: const Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(Icons.delete_sweep_rounded, color: AppTheme.red, size: 16),
                                SizedBox(width: 4),
                                Text(
                                  'Xóa tất cả',
                                  style: TextStyle(
                                    color: AppTheme.red,
                                    fontSize: 12,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),

              // Filter Chips
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                child: Row(
                  children: [
                    _filterChip('Tất cả', 'ALL'),
                    _filterChip('Kẻ đột nhập', 'INTRUDER'),
                    _filterChip('Bé nguy hiểm', 'BABY'),
                    _filterChip('Chưa xử lý', 'UNRESOLVED'),
                  ],
                ),
              ),

              // Alerts List
              if (_isLoading)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.all(40),
                    child: CircularProgressIndicator(color: AppTheme.blue),
                  ),
                )
              else if (_filteredAlerts.isEmpty)
                Container(
                  width: double.infinity,
                  margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  padding: const EdgeInsets.all(36),
                  decoration: BoxDecoration(
                    color: AppTheme.bg2,
                    borderRadius: BorderRadius.circular(AppTheme.cardRadius),
                    border: Border.all(color: Colors.white.withOpacity(0.05)),
                  ),
                  child: Column(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(18),
                        decoration: BoxDecoration(
                          color: AppTheme.blue.withOpacity(0.08),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.shield_outlined, color: AppTheme.blue, size: 44),
                      ),
                      const SizedBox(height: 16),
                      const Text(
                        'Hệ thống an toàn tuyệt đối',
                        style: TextStyle(
                          color: AppTheme.textPrimary,
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        'Không có sự cố nào được ghi nhận. Hệ thống AI đang liên tục giám sát bảo vệ ngôi nhà của bạn.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: AppTheme.textSecondary.withOpacity(0.8),
                          fontSize: 12.5,
                          height: 1.45,
                        ),
                      ),
                    ],
                  ),
                )
              else
                ListView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
                  itemCount: _filteredAlerts.length,
                  itemBuilder: (context, index) {
                    final item = _filteredAlerts[index];
                    return AlertCard(
                      key: ValueKey(item.id),
                      alert: item,
                      onResolve: () => _resolveAlert(item),
                      onDelete: () => _deleteAlert(item),
                    );
                  },
                ),

              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }

  Widget _demoButton(String label, IconData icon, Color color, VoidCallback onPressed) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onPressed,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: color.withOpacity(0.12),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: color.withOpacity(0.3)),
          ),
          child: Column(
            children: [
              Icon(icon, size: 20, color: color),
              const SizedBox(height: 5),
              Text(
                label,
                style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: color),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _filterChip(String label, String code) {
    final isSelected = _activeFilter == code;
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: () => setState(() => _activeFilter = code),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          decoration: BoxDecoration(
            gradient: isSelected ? AppTheme.blueGradient : null,
            color: isSelected ? null : AppTheme.bg2,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: isSelected ? Colors.transparent : Colors.white.withOpacity(0.08),
            ),
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: AppTheme.blue.withOpacity(0.35),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ]
                : [],
          ),
          child: Text(
            label,
            style: TextStyle(
              color: isSelected ? Colors.white : AppTheme.textSecondary,
              fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              fontSize: 12.5,
            ),
          ),
        ),
      ),
    );
  }
}

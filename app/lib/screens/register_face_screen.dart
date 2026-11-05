import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../services/api_service.dart';
import '../theme/app_theme.dart';

class RegisterFaceScreen extends StatefulWidget {
  const RegisterFaceScreen({super.key});
  @override
  State<RegisterFaceScreen> createState() => _RegisterFaceScreenState();
}

class _RegisterFaceScreenState extends State<RegisterFaceScreen>
    with TickerProviderStateMixin, WidgetsBindingObserver {

  // ── Camera ──────────────────────────────────────────────────
  CameraController? _ctrl;
  List<CameraDescription> _cameras = [];
  bool _cameraReady = false;
  int _camIdx = 1; // front camera default

  // ── State ───────────────────────────────────────────────────
  final int _total = 5;
  int _captured = 0;
  bool _scanning = false;   // auto-scan in progress
  bool _uploading = false;
  bool _isDone = false;
  String? _error;
  String _hint = 'Đặt khuôn mặt vào khung';

  // ── Name step ───────────────────────────────────────────────
  bool _nameConfirmed = false;
  final _nameCtrl = TextEditingController();

  // ── Registered list ─────────────────────────────────────────
  List<Map<String, dynamic>> _persons = [];
  bool _loadingPersons = false;

  // ── Animations ──────────────────────────────────────────────
  late AnimationController _scanLineCtrl;   // scan line sweep
  late AnimationController _pulseCtrl;      // oval pulse
  late AnimationController _flashCtrl;      // green flash on capture
  late Animation<double> _scanAnim;
  late Animation<double> _pulseAnim;
  late Animation<double> _flashAnim;

  // ── Auto-capture timer ──────────────────────────────────────
  Timer? _autoTimer;
  int _countdown = 3;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);

    // Scan line: 0 → 1 → 0, 2 seconds loop
    _scanLineCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2000),
    )..repeat(reverse: true);
    _scanAnim = CurvedAnimation(parent: _scanLineCtrl, curve: Curves.easeInOut);

    // Pulse: scale 1.0 → 1.03 → 1.0
    _pulseCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _pulseAnim = Tween<double>(begin: 1.0, end: 1.03).animate(
      CurvedAnimation(parent: _pulseCtrl, curve: Curves.easeInOut),
    );

    // Flash on capture
    _flashCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 400),
    );
    _flashAnim = Tween<double>(begin: 0, end: 1).animate(_flashCtrl);

    _loadPersons();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _scanLineCtrl.dispose();
    _pulseCtrl.dispose();
    _flashCtrl.dispose();
    _autoTimer?.cancel();
    _ctrl?.dispose();
    _nameCtrl.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive) _ctrl?.dispose();
    else if (state == AppLifecycleState.resumed && _nameConfirmed) _initCamera();
  }

  // ── Camera ──────────────────────────────────────────────────
  Future<void> _initCamera() async {
    // ① Xin quyền camera trước
    final status = await Permission.camera.request();
    if (!mounted) return;
    if (!status.isGranted) {
      setState(() => _error =
          'Cần cấp quyền camera!\nVào Cài đặt → Quyền → Camera → Cho phép');
      return;
    }

    try {
      _cameras = await availableCameras();
      if (_cameras.isEmpty) {
        setState(() => _error = 'Không tìm thấy camera!');
        return;
      }
      final idx = _camIdx < _cameras.length ? _camIdx : 0;
      final cam = _cameras[idx];
      final ctrl = CameraController(cam, ResolutionPreset.medium,
          enableAudio: false, imageFormatGroup: ImageFormatGroup.jpeg);
      await ctrl.initialize();
      if (!mounted) return;
      setState(() { _ctrl = ctrl; _cameraReady = true; });
      // Auto-start countdown after camera ready
      _startCountdown();
    } catch (e) {
      setState(() => _error = 'Lỗi camera: $e');
    }
  }

  Future<void> _switchCamera() async {
    if (_cameras.length < 2) return;
    _autoTimer?.cancel();
    _camIdx = (_camIdx + 1) % _cameras.length;
    await _ctrl?.dispose();
    setState(() { _ctrl = null; _cameraReady = false; _scanning = false; _countdown = 3; });
    await _initCamera();
  }

  // ── Auto-capture countdown ───────────────────────────────────
  void _startCountdown() {
    if (_isDone || _captured >= _total) return;
    setState(() { _countdown = 3; _scanning = false; _hint = 'Nhìn thẳng vào camera...'; });

    _autoTimer?.cancel();
    int count = 3;
    _autoTimer = Timer.periodic(const Duration(seconds: 1), (t) async {
      if (!mounted) { t.cancel(); return; }
      count--;
      if (count > 0) {
        setState(() { _countdown = count; _hint = 'Giữ nguyên... $count'; });
      } else {
        t.cancel();
        setState(() { _scanning = true; _hint = 'Đang quét khuôn mặt...'; });
        await _capturePhoto();
      }
    });
  }

  // ── Capture & upload ─────────────────────────────────────────
  Future<void> _capturePhoto() async {
    if (_ctrl == null || !_ctrl!.value.isInitialized) return;
    setState(() => _uploading = true);
    try {
      final xFile = await _ctrl!.takePicture();
      final bytes = await xFile.readAsBytes();
      final b64 = base64Encode(bytes);

      // Flash effect
      _flashCtrl.forward(from: 0).then((_) => _flashCtrl.reverse());

      final result = await ApiService.registerFacePhoto(
        name: _nameCtrl.text.trim(),
        base64Image: b64,
      );

      if (!mounted) return;
      if (result['success'] == true) {
        final newCount = _captured + 1;
        setState(() {
          _captured = newCount;
          _scanning = false;
          _uploading = false;
          if (newCount >= _total) {
            _isDone = true;
            _hint = 'Hoàn tất! 🎉';
          } else {
            _hint = 'Tuyệt! Còn ${_total - newCount} ảnh nữa';
          }
        });
        if (!_isDone) {
          await Future.delayed(const Duration(milliseconds: 800));
          if (mounted) _startCountdown();
        }
      } else {
        // Hiển thị lỗi rõ ràng hơn
        final errMsg = result['error']?.toString() ?? 'Lỗi không xác định';
        final isNetwork = errMsg.contains('Connection') ||
            errMsg.contains('timeout') ||
            errMsg.contains('refused') ||
            errMsg.contains('SocketException');
        setState(() {
          _error = isNetwork
              ? '❌ Không kết nối được backend!\nKiểm tra:\n• Backend đang chạy?\n• IP đúng chưa? (${ApiService.baseUrl})\n• Cùng WiFi?'
              : '❌ $errMsg';
          _scanning = false;
          _uploading = false;
          _hint = 'Nhấn ◉ để thử lại';
        });
      }
    } catch (e) {
      if (!mounted) return;
      final errStr = e.toString();
      final isNetwork = errStr.contains('SocketException') ||
          errStr.contains('Connection refused') ||
          errStr.contains('timeout');
      setState(() {
        _error = isNetwork
            ? '❌ Không kết nối được server!\nĐảm bảo backend đang chạy\nvà điện thoại cùng WiFi với laptop.'
            : '❌ Lỗi: $errStr';
        _scanning = false;
        _uploading = false;
        _hint = 'Nhấn ◉ để thử lại';
      });
    }
  }

  // ── Persons list ────────────────────────────────────────────
  Future<void> _loadPersons() async {
    setState(() => _loadingPersons = true);
    final p = await ApiService.fetchRegisteredFaces();
    if (mounted) setState(() { _persons = p; _loadingPersons = false; });
  }

  void _reset() {
    _autoTimer?.cancel();
    _ctrl?.dispose();
    setState(() {
      _captured = 0; _isDone = false; _error = null;
      _nameConfirmed = false; _cameraReady = false;
      _scanning = false; _countdown = 3;
      _hint = 'Đặt khuôn mặt vào khung';
      _ctrl = null;
    });
    _nameCtrl.clear();
    _loadPersons();
  }

  void _showEditServerDialog() {
    final ipCtrl = TextEditingController(text: ApiService.baseUrl);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1A1F2E),
        title: const Text('Cấu hình IP Server', style: TextStyle(color: Colors.white, fontSize: 18)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Nhập IP laptop của bạn (ví dụ: http://192.168.100.225:8080):',
              style: TextStyle(color: Colors.white70, fontSize: 13),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: ipCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                filled: true,
                fillColor: Colors.black26,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Hủy', style: TextStyle(color: Colors.white54)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppTheme.blue),
            onPressed: () {
              final newUrl = ipCtrl.text.trim();
              if (newUrl.isNotEmpty) {
                ApiService.setBaseUrl(newUrl);
                setState(() {});
                _loadPersons();
              }
              Navigator.pop(ctx);
            },
            child: const Text('Lưu'),
          ),
        ],
      ),
    );
  }

  void _confirmName() {
    if (_nameCtrl.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Vui lòng nhập tên'), backgroundColor: Colors.orange));
      return;
    }
    setState(() => _nameConfirmed = true);
    _initCamera();
  }

  // ── Build ────────────────────────────────────────────────────
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        title: const Text('Đăng ký Khuôn mặt',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          if (_nameConfirmed && _cameras.length > 1)
            IconButton(
              icon: const Icon(Icons.flip_camera_android, color: Colors.white70),
              onPressed: _switchCamera,
            ),
        ],
      ),
      body: _nameConfirmed ? _buildScanUI() : _buildNameUI(),
    );
  }

  // ════════════════════════════════════════════════════════════
  // STEP 1 — Name input
  // ════════════════════════════════════════════════════════════
  Widget _buildNameUI() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        // Input card
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: const Color(0xFF1A1F2E),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: AppTheme.blue.withOpacity(0.4)),
          ),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  gradient: LinearGradient(colors: [AppTheme.blue, AppTheme.purple]),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.face_retouching_natural, color: Colors.white, size: 26),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  const Text('Thêm thành viên',
                      style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                  GestureDetector(
                    onTap: _showEditServerDialog,
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Flexible(
                          child: Text(
                            'Server: ${ApiService.baseUrl}',
                            style: const TextStyle(
                              color: Colors.greenAccent,
                              fontSize: 11,
                              fontWeight: FontWeight.w600,
                              decoration: TextDecoration.underline,
                            ),
                          ),
                        ),
                        const SizedBox(width: 4),
                        const Icon(Icons.edit, color: Colors.greenAccent, size: 12),
                      ],
                    ),
                  ),
                ]),
              ),
            ]),
            const SizedBox(height: 20),
            TextField(
              controller: _nameCtrl,
              autofocus: true,
              textCapitalization: TextCapitalization.words,
              style: const TextStyle(color: Colors.white, fontSize: 16),
              decoration: InputDecoration(
                hintText: 'Nhập tên (Ba, Me, Anh, Chi...)',
                hintStyle: const TextStyle(color: Colors.white38),
                filled: true,
                fillColor: Colors.black26,
                prefixIcon: Icon(Icons.badge_outlined, color: AppTheme.blue),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: BorderSide.none),
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: BorderSide(color: AppTheme.blue, width: 2)),
              ),
              onSubmitted: (_) => _confirmName(),
            ),
            const SizedBox(height: 18),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _confirmName,
                icon: const Icon(Icons.radar),
                label: const Text('Bắt đầu quét khuôn mặt',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppTheme.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
              ),
            ),
          ]),
        ),

        const SizedBox(height: 28),
        // Registered persons
        Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          const Text('Đã đăng ký',
              style: TextStyle(color: Colors.white70, fontSize: 15, fontWeight: FontWeight.w600)),
          IconButton(
              icon: Icon(Icons.refresh, color: AppTheme.blue, size: 20),
              onPressed: _loadPersons),
        ]),
        const SizedBox(height: 8),
        if (_loadingPersons)
          const Center(child: CircularProgressIndicator(color: Colors.white38))
        else if (_persons.isEmpty)
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
                color: const Color(0xFF1A1F2E),
                borderRadius: BorderRadius.circular(12)),
            child: const Center(
                child: Text('Chưa có ai được đăng ký',
                    style: TextStyle(color: Colors.white38))),
          )
        else
          ..._persons.map((p) => _personTile(p)),
      ]),
    );
  }

  Widget _personTile(Map<String, dynamic> p) {
    final name = p['name']?.toString() ?? '';
    final count = p['photo_count'] ?? 0;
    final done = count >= 5;
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: const Color(0xFF1A1F2E),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
            color: (done ? Colors.greenAccent : Colors.orange).withOpacity(0.4)),
      ),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppTheme.blue.withOpacity(0.15),
          child: Text(name.isNotEmpty ? name[0].toUpperCase() : '?',
              style: TextStyle(color: AppTheme.blue, fontWeight: FontWeight.bold)),
        ),
        title: Text(name, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
        subtitle: Text('$count ảnh${done ? ' ✅' : ' — cần thêm'}',
            style: TextStyle(
                color: done ? Colors.greenAccent : Colors.orange, fontSize: 12)),
        trailing: IconButton(
          icon: const Icon(Icons.delete_outline, color: Colors.red),
          onPressed: () => _askDelete(name),
        ),
      ),
    );
  }

  Future<void> _askDelete(String name) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: const Color(0xFF1A1F2E),
        title: const Text('Xóa khuôn mặt?', style: TextStyle(color: Colors.white)),
        content: Text('Xóa toàn bộ ảnh của "$name"?',
            style: const TextStyle(color: Colors.white70)),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false),
              child: const Text('Hủy')),
          TextButton(onPressed: () => Navigator.pop(context, true),
              child: const Text('Xóa', style: TextStyle(color: Colors.red))),
        ],
      ),
    );
    if (ok == true) {
      await ApiService.deleteRegisteredFace(name);
      await _loadPersons();
    }
  }

  // ════════════════════════════════════════════════════════════
  // STEP 2 — Face scanner UI
  // ════════════════════════════════════════════════════════════
  Widget _buildScanUI() {
    if (_isDone) return _buildDoneUI();

    // Color theme based on state
    final Color frameColor = _uploading
        ? Colors.greenAccent
        : _scanning
            ? AppTheme.blue
            : Colors.white54;

    return Stack(children: [
      // ── Camera preview full screen ──────────────────────────
      if (_cameraReady && _ctrl != null)
        Positioned.fill(
          child: CameraPreview(_ctrl!),
        )
      else
        Positioned.fill(
          child: Container(
            color: Colors.black,
            child: _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(28),
                      child: Column(mainAxisSize: MainAxisSize.min, children: [
                        const Icon(Icons.error_outline, color: Colors.redAccent, size: 56),
                        const SizedBox(height: 16),
                        Text(
                          _error!,
                          textAlign: TextAlign.center,
                          style: const TextStyle(color: Colors.white, fontSize: 14, height: 1.6),
                        ),
                        const SizedBox(height: 24),
                        if (_error!.contains('quyền'))
                          ElevatedButton.icon(
                            onPressed: () => openAppSettings(),
                            icon: const Icon(Icons.settings),
                            label: const Text('Mở Cài đặt → Cấp quyền'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppTheme.blue,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(12)),
                            ),
                          )
                        else
                          ElevatedButton.icon(
                            onPressed: () {
                              setState(() => _error = null);
                              _startCountdown();
                            },
                            icon: const Icon(Icons.refresh),
                            label: const Text('Thử lại'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppTheme.blue,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(12)),
                            ),
                          ),
                      ]),
                    ),
                  )
                : const Center(child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      CircularProgressIndicator(color: Colors.white38),
                      SizedBox(height: 14),
                      Text('Đang khởi động camera...', style: TextStyle(color: Colors.white38)),
                    ],
                  )),
          ),
        ),

      // ── Dark overlay with oval cutout ───────────────────────
      if (_cameraReady)
        Positioned.fill(
          child: AnimatedBuilder(
            animation: _pulseAnim,
            builder: (_, __) => CustomPaint(
              painter: _FaceScanOverlayPainter(
                frameColor: frameColor,
                pulse: _pulseAnim.value,
                scanning: _scanning,
                scanValue: _scanAnim.value,
              ),
            ),
          ),
        ),

      // ── Flash overlay on capture ────────────────────────────
      AnimatedBuilder(
        animation: _flashAnim,
        builder: (_, __) => Opacity(
          opacity: _flashAnim.value * 0.6,
          child: Container(color: Colors.white),
        ),
      ),

      // ── Top: name + progress dots ───────────────────────────
      Positioned(
        top: 0, left: 0, right: 0,
        child: Container(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 20),
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter, end: Alignment.bottomCenter,
              colors: [Colors.black87, Colors.transparent],
            ),
          ),
          child: Column(children: [
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              Text(_nameCtrl.text.trim(),
                  style: const TextStyle(
                      color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
            ]),
            const SizedBox(height: 10),
            Row(mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(_total, (i) {
                final done = i < _captured;
                final active = i == _captured && _scanning;
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 400),
                  margin: const EdgeInsets.symmetric(horizontal: 5),
                  width: active ? 28 : (done ? 20 : 14),
                  height: 8,
                  decoration: BoxDecoration(
                    color: done
                        ? Colors.greenAccent
                        : active
                            ? AppTheme.blue
                            : Colors.white24,
                    borderRadius: BorderRadius.circular(4),
                  ),
                );
              }),
            ),
          ]),
        ),
      ),

      // ── Countdown circle (center, below oval) ──────────────
      if (_cameraReady && !_scanning && !_uploading && _captured < _total)
        Center(
          child: Transform.translate(
            offset: Offset(0, MediaQuery.of(context).size.height * 0.26),
            child: _buildCountdownBadge(),
          ),
        ),

      // ── Hint text at bottom ─────────────────────────────────
      Positioned(
        bottom: 0, left: 0, right: 0,
        child: Container(
          padding: const EdgeInsets.fromLTRB(20, 30, 20, 50),
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.bottomCenter, end: Alignment.topCenter,
              colors: [Colors.black87, Colors.transparent],
            ),
          ),
          child: Column(children: [
            AnimatedSwitcher(
              duration: const Duration(milliseconds: 300),
              child: Text(
                _error ?? _hint,
                key: ValueKey(_error ?? _hint),
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _error != null ? Colors.redAccent : Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            const SizedBox(height: 20),
            // Manual capture button (tap anytime) — or retry on error
            if (_error != null && !_uploading)
              ElevatedButton.icon(
                onPressed: () {
                  setState(() => _error = null);
                  _startCountdown();
                },
                icon: const Icon(Icons.refresh),
                label: const Text('Thử lại'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.redAccent,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              )
            else if (!_uploading)
              GestureDetector(
                onTap: () {
                  _autoTimer?.cancel();
                  setState(() { _scanning = true; _hint = 'Đang quét...'; _countdown = 0; });
                  _capturePhoto();
                },
                child: Container(
                  width: 70, height: 70,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: Colors.white54, width: 3),
                    color: Colors.white10,
                  ),
                  child: const Icon(Icons.touch_app, color: Colors.white70, size: 30),
                ),
              )
            else
              const SizedBox(
                width: 70, height: 70,
                child: CircularProgressIndicator(color: Colors.greenAccent, strokeWidth: 3),
              ),
            const SizedBox(height: 8),
            Text(
              _error != null ? '' : 'Tự động hoặc nhấn để chụp',
              style: const TextStyle(color: Colors.white38, fontSize: 11),
            ),
          ]),
        ),
      ),

      // ── Switch camera button ────────────────────────────────
      if (_cameras.length > 1)
        Positioned(
          right: 20, top: 100,
          child: GestureDetector(
            onTap: _switchCamera,
            child: Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: Colors.black45,
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white24),
              ),
              child: const Icon(Icons.flip_camera_android, color: Colors.white70, size: 22),
            ),
          ),
        ),
    ]);
  }

  Widget _buildCountdownBadge() {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 200),
      child: Container(
        key: ValueKey(_countdown),
        width: 56, height: 56,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: Colors.black54,
          border: Border.all(color: AppTheme.blue, width: 2),
        ),
        child: Center(
          child: Text('$_countdown',
              style: TextStyle(
                  color: AppTheme.blue,
                  fontSize: 26,
                  fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }

  // ════════════════════════════════════════════════════════════
  // STEP 3 — Done
  // ════════════════════════════════════════════════════════════
  Widget _buildDoneUI() {
    return Container(
      color: Colors.black,
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            // Success animation ring
            Container(
              width: 120, height: 120,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: Colors.greenAccent, width: 3),
                color: Colors.greenAccent.withOpacity(0.1),
              ),
              child: const Icon(Icons.check, color: Colors.greenAccent, size: 60),
            ),
            const SizedBox(height: 28),
            const Text('Đăng ký thành công!',
                style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
            const SizedBox(height: 10),
            Text(
              '"${_nameCtrl.text.trim()}" đã được lưu $_total ảnh\nvào thư mục family trên laptop.',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white60, fontSize: 15),
            ),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: Colors.greenAccent.withOpacity(0.1),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: Colors.greenAccent.withOpacity(0.3)),
              ),
              child: const Text(
                '✅ Chạy lại demo.py --source 0 --family family\nđể nhận diện khuôn mặt qua webcam laptop',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.greenAccent, fontSize: 12),
              ),
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: _reset,
              icon: const Icon(Icons.person_add),
              label: const Text('Đăng ký thêm người'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.blue,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('← Quay về trang chủ',
                  style: TextStyle(color: Colors.white38)),
            ),
          ]),
        ),
      ),
    );
  }
}

// ══════════════════════════════════════════════════════════════
// Custom Painter — Face scan overlay
// ══════════════════════════════════════════════════════════════
class _FaceScanOverlayPainter extends CustomPainter {
  final Color frameColor;
  final double pulse;
  final bool scanning;
  final double scanValue; // 0.0 → 1.0

  _FaceScanOverlayPainter({
    required this.frameColor,
    required this.pulse,
    required this.scanning,
    required this.scanValue,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final cx = size.width / 2;
    final cy = size.height * 0.42;
    final rx = size.width * 0.34 * pulse;
    final ry = size.height * 0.265 * pulse;
    final ovalRect = Rect.fromCenter(
        center: Offset(cx, cy), width: rx * 2, height: ry * 2);

    // ── Dark overlay with oval hole ─────────────────────────
    final overlayPaint = Paint()..color = Colors.black.withOpacity(0.55);
    final path = Path()
      ..addRect(Rect.fromLTWH(0, 0, size.width, size.height))
      ..addOval(ovalRect)
      ..fillType = PathFillType.evenOdd;
    canvas.drawPath(path, overlayPaint);

    // ── Oval frame ──────────────────────────────────────────
    final framePaint = Paint()
      ..color = frameColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.5;
    canvas.drawOval(ovalRect, framePaint);

    // ── Corner arcs (like banking app) ──────────────────────
    _drawCornerArcs(canvas, ovalRect, frameColor);

    // ── Scan line (only while scanning) ─────────────────────
    if (scanning) {
      final lineY = ovalRect.top + ovalRect.height * scanValue;
      // Clip scan line inside oval
      canvas.save();
      final clipPath = Path()..addOval(ovalRect);
      canvas.clipPath(clipPath);

      final gradient = LinearGradient(
        colors: [
          Colors.transparent,
          frameColor.withOpacity(0.8),
          frameColor,
          frameColor.withOpacity(0.8),
          Colors.transparent,
        ],
        stops: const [0.0, 0.2, 0.5, 0.8, 1.0],
      );
      final linePaint = Paint()
        ..shader = gradient.createShader(
            Rect.fromLTWH(ovalRect.left, lineY - 2, ovalRect.width, 4))
        ..strokeWidth = 3;
      canvas.drawLine(Offset(ovalRect.left, lineY), Offset(ovalRect.right, lineY), linePaint);

      // Glow below scan line
      final glowPaint = Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [frameColor.withOpacity(0.15), Colors.transparent],
        ).createShader(Rect.fromLTWH(ovalRect.left, lineY, ovalRect.width, 40));
      canvas.drawRect(
          Rect.fromLTWH(ovalRect.left, lineY, ovalRect.width, 40), glowPaint);

      canvas.restore();
    }
  }

  void _drawCornerArcs(Canvas canvas, Rect oval, Color color) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.5
      ..strokeCap = StrokeCap.round;

    const arcLen = 0.35; // radians

    // Top-left
    canvas.drawArc(oval, math.pi + 0.1, arcLen, false, paint);
    // Top-right
    canvas.drawArc(oval, -0.1 - arcLen, arcLen, false, paint);
    // Bottom-left
    canvas.drawArc(oval, math.pi - arcLen - 0.1, arcLen, false, paint);
    // Bottom-right
    canvas.drawArc(oval, 0.1, arcLen, false, paint);
  }

  @override
  bool shouldRepaint(_FaceScanOverlayPainter old) =>
      old.frameColor != frameColor ||
      old.pulse != pulse ||
      old.scanning != scanning ||
      old.scanValue != scanValue;
}

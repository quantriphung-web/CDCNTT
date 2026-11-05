import 'dart:convert';
import 'package:flutter/material.dart';
import '../models/alert_item.dart';
import '../theme/app_theme.dart';

class AlertDetailDialog extends StatelessWidget {
  final AlertItem alert;
  final VoidCallback onResolve;
  final VoidCallback onDelete;

  const AlertDetailDialog({
    super.key,
    required this.alert,
    required this.onResolve,
    required this.onDelete,
  });

  Color get _color {
    if (alert.isCritical) return AppTheme.red;
    if (alert.severity == 'WARNING') return AppTheme.amber;
    return AppTheme.blue;
  }

  LinearGradient get _gradient {
    if (alert.isCritical) return AppTheme.redGradient;
    if (alert.severity == 'WARNING') return AppTheme.amberGradient;
    return AppTheme.blueGradient;
  }

  IconData get _icon {
    final t = alert.alertType.toLowerCase();
    if (t.contains('fire') || t.contains('smoke')) return Icons.local_fire_department_rounded;
    if (t.contains('intruder') || t.contains('theft')) return Icons.security_rounded;
    if (t.contains('baby') || t.contains('child')) return Icons.child_care_rounded;
    if (t.contains('door') || t.contains('gate')) return Icons.door_front_door_outlined;
    if (t.contains('pet')) return Icons.pets_rounded;
    if (t.contains('elder') || t.contains('fall')) return Icons.elderly_rounded;
    return Icons.warning_amber_rounded;
  }

  @override
  Widget build(BuildContext context) {
    final color = _color;

    return Dialog(
      backgroundColor: AppTheme.bg2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppTheme.cardRadius),
        side: BorderSide(color: color.withOpacity(0.4), width: 1.5),
      ),
      insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 24),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 480),
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(22),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        gradient: _gradient,
                        borderRadius: BorderRadius.circular(16),
                        boxShadow: [
                          BoxShadow(
                            color: color.withOpacity(0.35),
                            blurRadius: 10,
                            offset: const Offset(0, 3),
                          ),
                        ],
                      ),
                      child: Icon(_icon, color: Colors.white, size: 24),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: color.withOpacity(0.18),
                                  borderRadius: BorderRadius.circular(6),
                                  border: Border.all(color: color.withOpacity(0.35)),
                                ),
                                child: Text(
                                  alert.severity,
                                  style: TextStyle(
                                    color: color,
                                    fontSize: 10.5,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: 0.8,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text(
                            alert.timeFormatted,
                            style: const TextStyle(
                              color: AppTheme.textSecondary,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close_rounded, color: Colors.white60),
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Title
                Text(
                  alert.title,
                  style: const TextStyle(
                    color: AppTheme.textPrimary,
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.2,
                  ),
                ),
                const SizedBox(height: 8),

                // Message
                Text(
                  alert.message,
                  style: TextStyle(
                    color: AppTheme.textSecondary.withOpacity(0.9),
                    fontSize: 13.5,
                    height: 1.45,
                  ),
                ),
                const SizedBox(height: 18),

                // Snapshot Image Preview
                if (alert.imageBase64 != null && alert.imageBase64!.isNotEmpty) ...[
                  ClipRRect(
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.white.withOpacity(0.12)),
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Image.memory(
                        base64Decode(alert.imageBase64!),
                        fit: BoxFit.cover,
                        width: double.infinity,
                        errorBuilder: (ctx, err, stack) => Container(
                          height: 160,
                          color: Colors.black26,
                          child: const Center(
                            child: Text('Lỗi tải ảnh snapshot', style: TextStyle(color: Colors.white54)),
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 18),
                ],

                // Metadata Details
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.04),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: Colors.white.withOpacity(0.06)),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _metaItem('Mã sự cố', '#${alert.id}'),
                      if (alert.trackId != null)
                        _metaItem('Track ID', '${alert.trackId}'),
                      _metaItem('Trạng thái', alert.isResolved ? '✓ Đã xử lý' : '⏳ Đang chờ'),
                    ],
                  ),
                ),
                const SizedBox(height: 22),

                // Actions
                if (!alert.isResolved)
                  Container(
                    width: double.infinity,
                    decoration: BoxDecoration(
                      gradient: AppTheme.greenGradient,
                      borderRadius: BorderRadius.circular(12),
                      boxShadow: [
                        BoxShadow(
                          color: AppTheme.green.withOpacity(0.3),
                          blurRadius: 10,
                          offset: const Offset(0, 3),
                        ),
                      ],
                    ),
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.transparent,
                        shadowColor: Colors.transparent,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      icon: const Icon(Icons.check_circle_outline_rounded, size: 20),
                      label: const Text('Đánh dấu đã xử lý', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                      onPressed: () {
                        onResolve();
                        Navigator.pop(context);
                      },
                    ),
                  ),
                if (!alert.isResolved) const SizedBox(height: 12),

                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        style: OutlinedButton.styleFrom(
                          foregroundColor: AppTheme.textSecondary,
                          side: BorderSide(color: Colors.white.withOpacity(0.15)),
                          padding: const EdgeInsets.symmetric(vertical: 13),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Đóng', style: TextStyle(fontWeight: FontWeight.w600)),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppTheme.red,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 13),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        icon: const Icon(Icons.delete_outline_rounded, size: 18),
                        label: const Text('Xóa sự cố', style: TextStyle(fontWeight: FontWeight.bold)),
                        onPressed: onDelete,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _metaItem(String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: TextStyle(color: AppTheme.textSecondary.withOpacity(0.7), fontSize: 11),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(color: AppTheme.textPrimary, fontSize: 13, fontWeight: FontWeight.bold),
        ),
      ],
    );
  }
}

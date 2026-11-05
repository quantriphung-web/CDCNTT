import 'dart:convert';
import 'package:flutter/material.dart';
import '../models/alert_item.dart';
import '../theme/app_theme.dart';
import 'alert_detail_dialog.dart';

class AlertCard extends StatelessWidget {
  final AlertItem alert;
  final VoidCallback onResolve;
  final VoidCallback onDelete;

  const AlertCard({
    super.key,
    required this.alert,
    required this.onResolve,
    required this.onDelete,
  });

  Color get _primaryColor {
    if (alert.isCritical) return AppTheme.red;
    if (alert.severity == 'WARNING') return AppTheme.amber;
    return AppTheme.blue;
  }

  LinearGradient get _primaryGradient {
    if (alert.isCritical) return AppTheme.redGradient;
    if (alert.severity == 'WARNING') return AppTheme.amberGradient;
    return AppTheme.blueGradient;
  }

  IconData get _alertIcon {
    final t = alert.alertType.toLowerCase();
    if (t.contains('fire') || t.contains('smoke')) return Icons.local_fire_department_rounded;
    if (t.contains('baby') || t.contains('child')) return Icons.child_care_rounded;
    if (t.contains('intruder') || t.contains('theft')) return Icons.person_off_rounded;
    if (t.contains('door') || t.contains('gate')) return Icons.door_front_door_outlined;
    if (t.contains('pet')) return Icons.pets_rounded;
    if (t.contains('elder') || t.contains('fall')) return Icons.elderly_rounded;
    if (t.contains('stair')) return Icons.stairs_rounded;
    return Icons.notifications_active_rounded;
  }

  @override
  Widget build(BuildContext context) {
    final color = _primaryColor;

    return Dismissible(
      key: ValueKey(alert.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 22),
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [Colors.transparent, AppTheme.red.withOpacity(0.9)],
            begin: Alignment.centerLeft,
            end: Alignment.centerRight,
          ),
          borderRadius: BorderRadius.circular(AppTheme.cardRadius),
        ),
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.delete_sweep_rounded, color: Colors.white, size: 28),
            SizedBox(height: 4),
            Text('Xóa', style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold)),
          ],
        ),
      ),
      confirmDismiss: (_) async {
        return await _showDeleteConfirm(context);
      },
      onDismissed: (_) => onDelete(),
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: AppTheme.bg2,
          borderRadius: BorderRadius.circular(AppTheme.cardRadius),
          border: Border.all(
            color: alert.isResolved
                ? Colors.white.withOpacity(0.06)
                : color.withOpacity(0.35),
            width: alert.isResolved ? 1.0 : 1.4,
          ),
          boxShadow: alert.isResolved
              ? []
              : [
                  BoxShadow(
                    color: color.withOpacity(0.12),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  )
                ],
        ),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(AppTheme.cardRadius),
            onTap: () {
              showDialog(
                context: context,
                builder: (ctx) => AlertDetailDialog(
                  alert: alert,
                  onResolve: onResolve,
                  onDelete: () {
                    Navigator.pop(ctx);
                    onDelete();
                  },
                ),
              );
            },
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Snapshot preview or gradient icon
                  if (alert.imageBase64 != null && alert.imageBase64!.isNotEmpty)
                    ClipRRect(
                      borderRadius: BorderRadius.circular(14),
                      child: Container(
                        width: 60,
                        height: 60,
                        decoration: BoxDecoration(
                          border: Border.all(color: color.withOpacity(0.4), width: 1.2),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Stack(
                          fit: StackFit.expand,
                          children: [
                            Image.memory(
                              base64Decode(alert.imageBase64!),
                              fit: BoxFit.cover,
                              errorBuilder: (c, e, s) => Container(
                                color: color.withOpacity(0.15),
                                child: Icon(Icons.camera_alt_rounded, color: color, size: 24),
                              ),
                            ),
                            Positioned(
                              bottom: 3,
                              right: 3,
                              child: Container(
                                padding: const EdgeInsets.all(3),
                                decoration: BoxDecoration(
                                  color: Colors.black.withOpacity(0.6),
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(Icons.zoom_in_rounded, color: Colors.white, size: 10),
                              ),
                            )
                          ],
                        ),
                      ),
                    )
                  else
                    Container(
                      width: 52,
                      height: 52,
                      decoration: BoxDecoration(
                        gradient: _primaryGradient,
                        borderRadius: BorderRadius.circular(14),
                        boxShadow: [
                          BoxShadow(
                            color: color.withOpacity(0.3),
                            blurRadius: 8,
                            offset: const Offset(0, 2),
                          ),
                        ],
                      ),
                      child: Icon(_alertIcon, color: Colors.white, size: 24),
                    ),

                  const SizedBox(width: 14),

                  // Info
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                              decoration: BoxDecoration(
                                color: color.withOpacity(0.18),
                                borderRadius: BorderRadius.circular(6),
                                border: Border.all(color: color.withOpacity(0.3)),
                              ),
                              child: Text(
                                alert.severity,
                                style: TextStyle(
                                  color: color,
                                  fontSize: 10,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: 0.6,
                                ),
                              ),
                            ),
                            Text(
                              alert.timeAgo,
                              style: TextStyle(
                                color: AppTheme.textSecondary.withOpacity(0.7),
                                fontSize: 11,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 7),
                        Text(
                          alert.title,
                          style: TextStyle(
                            color: alert.isResolved
                                ? AppTheme.textSecondary
                                : AppTheme.textPrimary,
                            fontSize: 14.5,
                            fontWeight: FontWeight.bold,
                            decoration: alert.isResolved ? TextDecoration.lineThrough : null,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          alert.message,
                          style: const TextStyle(
                            color: AppTheme.textSecondary,
                            fontSize: 12,
                            height: 1.35,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(width: 8),

                  // Quick Action Buttons
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (!alert.isResolved)
                        Material(
                          color: Colors.transparent,
                          child: InkWell(
                            borderRadius: BorderRadius.circular(10),
                            onTap: onResolve,
                            child: Container(
                              padding: const EdgeInsets.all(7),
                              decoration: BoxDecoration(
                                color: AppTheme.green.withOpacity(0.15),
                                borderRadius: BorderRadius.circular(10),
                                border: Border.all(color: AppTheme.green.withOpacity(0.3)),
                              ),
                              child: const Icon(Icons.check_circle_rounded, color: AppTheme.green, size: 20),
                            ),
                          ),
                        )
                      else
                        Container(
                          padding: const EdgeInsets.all(7),
                          child: const Icon(Icons.check_circle_outline_rounded, color: Colors.white24, size: 20),
                        ),
                      const SizedBox(height: 6),
                      Material(
                        color: Colors.transparent,
                        child: InkWell(
                          borderRadius: BorderRadius.circular(10),
                          onTap: () async {
                            final confirm = await _showDeleteConfirm(context);
                            if (confirm == true) onDelete();
                          },
                          child: Container(
                            padding: const EdgeInsets.all(7),
                            decoration: BoxDecoration(
                              color: AppTheme.red.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(color: AppTheme.red.withOpacity(0.25)),
                            ),
                            child: const Icon(Icons.delete_outline_rounded, color: AppTheme.red, size: 20),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<bool?> _showDeleteConfirm(BuildContext context) {
    return showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.bg2,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppTheme.cardRadius),
          side: BorderSide(color: Colors.white.withOpacity(0.08)),
        ),
        title: const Row(
          children: [
            Icon(Icons.delete_outline_rounded, color: AppTheme.red, size: 22),
            SizedBox(width: 10),
            Text('Xóa thông báo?', style: TextStyle(color: AppTheme.textPrimary, fontSize: 16, fontWeight: FontWeight.bold)),
          ],
        ),
        content: const Text(
          'Thao tác này sẽ xóa vĩnh viễn cảnh báo khỏi hệ thống.',
          style: TextStyle(color: AppTheme.textSecondary, fontSize: 13),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Hủy', style: TextStyle(color: AppTheme.textSecondary)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.red,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            ),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Xóa', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

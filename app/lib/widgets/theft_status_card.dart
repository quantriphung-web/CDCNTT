import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class TheftStatusCard extends StatelessWidget {
  final String status;
  final String statusColor;

  const TheftStatusCard({
    super.key,
    required this.status,
    required this.statusColor,
  });

  @override
  Widget build(BuildContext context) {
    final isIntruder = status.contains('PHAT HIEN') ||
        status.contains('NGUOI LA') ||
        statusColor == 'RED';
    final isUnverified =
        status.contains('CHUA XAC MINH') || statusColor == 'ORANGE';

    final Color primaryColor = isIntruder
        ? AppTheme.red
        : isUnverified
            ? AppTheme.amber
            : AppTheme.green;

    final LinearGradient gradient = isIntruder
        ? AppTheme.redGradient
        : isUnverified
            ? AppTheme.amberGradient
            : AppTheme.greenGradient;

    final IconData iconData = isIntruder
        ? Icons.warning_amber_rounded
        : isUnverified
            ? Icons.help_outline_rounded
            : Icons.verified_user_rounded;

    final String badgeText = isIntruder
        ? 'BÁO ĐỘNG'
        : isUnverified
            ? 'CẢNH BÁO'
            : 'AN TOÀN';

    final String descText = isIntruder
        ? 'Phát hiện đối tượng không nhận diện được mặt! Ảnh chụp đã được ghi lại vào hệ thống.'
        : isUnverified
            ? 'Có đối tượng xuất hiện nhưng chưa xác minh được khuôn mặt quá 8 giây.'
            : 'Không phát hiện đối tượng lạ. AI nhận diện khuôn mặt người nhà hoạt động ổn định.';

    return Container(
      decoration: BoxDecoration(
        color: AppTheme.bg2,
        borderRadius: BorderRadius.circular(AppTheme.cardRadius),
        border: Border.all(
          color: primaryColor.withOpacity(isIntruder ? 0.6 : 0.2),
          width: isIntruder ? 1.8 : 1.0,
        ),
        boxShadow: [
          BoxShadow(
            color: primaryColor.withOpacity(isIntruder ? 0.22 : 0.06),
            blurRadius: isIntruder ? 20 : 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(AppTheme.cardRadius),
        child: Stack(
          children: [
            // Ambient glow corner
            Positioned(
              top: -30,
              right: -30,
              child: Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: primaryColor.withOpacity(0.08),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              gradient: gradient,
                              borderRadius: BorderRadius.circular(14),
                              boxShadow: [
                                BoxShadow(
                                  color: primaryColor.withOpacity(0.3),
                                  blurRadius: 8,
                                  offset: const Offset(0, 2),
                                ),
                              ],
                            ),
                            child: Icon(iconData, color: Colors.white, size: 22),
                          ),
                          const SizedBox(width: 12),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text(
                                'Chống Trộm AI',
                                style: TextStyle(
                                  color: AppTheme.textPrimary,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                  letterSpacing: 0.3,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                'Khuôn mặt & Đột nhập',
                                style: TextStyle(
                                  color: AppTheme.textSecondary.withOpacity(0.8),
                                  fontSize: 11,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                        decoration: BoxDecoration(
                          color: primaryColor.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: primaryColor.withOpacity(0.35)),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Container(
                              width: 6,
                              height: 6,
                              decoration: BoxDecoration(
                                color: primaryColor,
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: primaryColor.withOpacity(0.6),
                                    blurRadius: 4,
                                    spreadRadius: 1,
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(width: 6),
                            Text(
                              badgeText,
                              style: TextStyle(
                                color: primaryColor,
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    status,
                    style: TextStyle(
                      color: primaryColor,
                      fontSize: 17,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 0.2,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    descText,
                    style: const TextStyle(
                      color: AppTheme.textSecondary,
                      fontSize: 12.5,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class BabyStatusCard extends StatelessWidget {
  final String status;
  final String statusColor;

  const BabyStatusCard({
    super.key,
    required this.status,
    required this.statusColor,
  });

  @override
  Widget build(BuildContext context) {
    final isDanger = status.contains('NGUY CO') || statusColor == 'RED';
    final isSleeping = status.contains('NGU') || statusColor == 'BLUE';

    final Color primaryColor = isDanger
        ? AppTheme.red
        : isSleeping
            ? AppTheme.blue
            : AppTheme.green;

    final LinearGradient gradient = isDanger
        ? AppTheme.redGradient
        : isSleeping
            ? AppTheme.blueGradient
            : AppTheme.greenGradient;

    final IconData iconData = isDanger
        ? Icons.warning_amber_rounded
        : isSleeping
            ? Icons.bedtime_rounded
            : Icons.child_care_rounded;

    final String badgeText = isDanger
        ? 'NGUY HIỂM'
        : isSleeping
            ? 'ĐANG NGỦ'
            : 'HOẠT ĐỘNG';

    final String descText = isDanger
        ? 'Cảnh báo: Bé đã ra ngoài vùng an toàn 15% mép giường! Hãy kiểm tra ngay.'
        : isSleeping
            ? 'Bé đang ngủ ngoan trong nôi an toàn. AI giám sát cử động liên tục.'
            : 'Bé đang thức và cử động trong vùng giường an toàn.';

    return Container(
      decoration: BoxDecoration(
        color: AppTheme.bg2,
        borderRadius: BorderRadius.circular(AppTheme.cardRadius),
        border: Border.all(
          color: primaryColor.withOpacity(isDanger ? 0.6 : 0.2),
          width: isDanger ? 1.8 : 1.0,
        ),
        boxShadow: [
          BoxShadow(
            color: primaryColor.withOpacity(isDanger ? 0.22 : 0.06),
            blurRadius: isDanger ? 20 : 12,
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
                                'Trông Trẻ AI',
                                style: TextStyle(
                                  color: AppTheme.textPrimary,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                  letterSpacing: 0.3,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                'Vùng nguy hiểm & Cử động',
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

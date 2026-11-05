import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';


class NotificationService {
  static final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  static bool _initialized = false;

  static Future<void> initialize() async {
    if (_initialized) return;

    const AndroidInitializationSettings androidSettings =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    const DarwinInitializationSettings iosSettings =
        DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    const InitializationSettings settings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _plugin.initialize(
      settings,
      onDidReceiveNotificationResponse: (details) {
        // Handle tap on notification - could navigate to alerts screen
        debugPrint('[Notification] Tapped: ${details.payload}');
      },
    );

    // Request Android 13+ permission
    final androidImpl = _plugin.resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin>();
    if (androidImpl != null) {
      await androidImpl.requestNotificationsPermission();
    }

    _initialized = true;
    debugPrint('[NotificationService] Initialized');
  }

  /// Show a push notification for a security alert
  static Future<void> showAlertNotification({
    required int id,
    required String title,
    required String body,
    required bool isCritical,
    String? alertType,
  }) async {
    if (!_initialized) await initialize();

    // Skip on web platform
    if (kIsWeb) return;

    final androidDetails = AndroidNotificationDetails(
      isCritical ? 'critical_alerts' : 'general_alerts',
      isCritical ? 'Cảnh báo khẩn cấp' : 'Thông báo chung',
      channelDescription: isCritical
          ? 'Cảnh báo nghiêm trọng cần xử lý ngay'
          : 'Thông báo giám sát thông thường',
      importance: isCritical ? Importance.max : Importance.high,
      priority: isCritical ? Priority.max : Priority.high,
      icon: '@mipmap/ic_launcher',
      color: isCritical
          ? const Color(0xFFFF3B30)
          : const Color(0xFFFF9500),
      enableVibration: true,
      playSound: true,
      // Full-screen intent for critical alerts (shows on locked screen)
      fullScreenIntent: isCritical,
      ticker: title,
      styleInformation: BigTextStyleInformation(body),
      category: isCritical
          ? AndroidNotificationCategory.alarm
          : AndroidNotificationCategory.event,
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
      interruptionLevel: InterruptionLevel.timeSensitive,
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _plugin.show(
      id % 1000, // Notification ID (capped to avoid overflow)
      _buildNotificationTitle(alertType, title),
      body,
      details,
      payload: 'alert_$id',
    );
  }

  /// Show a grouped summary notification
  static Future<void> showSummaryNotification(int count) async {
    if (!_initialized || kIsWeb) return;

    const androidDetails = AndroidNotificationDetails(
      'general_alerts',
      'Thông báo chung',
      channelDescription: 'Thông báo giám sát thông thường',
      importance: Importance.defaultImportance,
      priority: Priority.defaultPriority,
      groupKey: 'baby_security_alerts',
      setAsGroupSummary: true,
    );

    const details = NotificationDetails(android: androidDetails);

    await _plugin.show(
      0,
      'Baby & SafeHome AI',
      '$count cảnh báo mới chưa xử lý',
      details,
    );
  }

  static String _buildNotificationTitle(String? alertType, String fallback) {
    if (alertType == null) return fallback;
    final t = alertType.toLowerCase();
    if (t.contains('intruder') || t.contains('theft')) return '🚨 Phát hiện kẻ xâm nhập!';
    if (t.contains('fire') || t.contains('smoke')) return '🔥 Cảnh báo khói/lửa!';
    if (t.contains('baby') || t.contains('child')) return '👶 Nguy hiểm cho bé!';
    if (t.contains('elder') || t.contains('fall')) return '🆘 Người già có thể ngã!';
    if (t.contains('pet')) return '🐾 Thú cưng ra ngoài!';
    if (t.contains('door') || t.contains('gate')) return '🚪 Cửa/cổng mở bất thường!';
    return fallback;
  }

  static Future<void> cancelAll() async {
    if (!_initialized || kIsWeb) return;
    await _plugin.cancelAll();
  }
}

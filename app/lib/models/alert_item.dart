import 'package:intl/intl.dart';

class AlertItem {
  final int id;
  final String alertType;
  final String title;
  final String message;
  final String severity; // CRITICAL, WARNING, INFO
  final String? imageBase64;
  final String? imagePath;
  final int? trackId;
  bool isRead;
  bool isResolved;
  final DateTime createdAt;

  AlertItem({
    required this.id,
    required this.alertType,
    required this.title,
    required this.message,
    required this.severity,
    this.imageBase64,
    this.imagePath,
    this.trackId,
    this.isRead = false,
    this.isResolved = false,
    required this.createdAt,
  });

  bool get isCritical => severity == 'CRITICAL';
  bool get isIntruder =>
      alertType == 'intruder' || alertType == 'stranger' || alertType == 'theft';
  bool get isBabyDanger =>
      alertType == 'baby_danger' || alertType == 'fall';
  bool get isUnverified => alertType == 'unverified';
  bool get isBabyWake => alertType == 'baby_wake' || alertType == 'wake';

  String get timeFormatted {
    return DateFormat('HH:mm:ss - dd/MM/yyyy').format(createdAt.toLocal());
  }

  String get timeAgo {
    final diff = DateTime.now().difference(createdAt);
    if (diff.inSeconds < 60) {
      return 'Vừa xong';
    } else if (diff.inMinutes < 60) {
      return '${diff.inMinutes} phút trước';
    } else if (diff.inHours < 24) {
      return '${diff.inHours} giờ trước';
    } else {
      return '${diff.inDays} ngày trước';
    }
  }

  factory AlertItem.fromJson(Map<String, dynamic> json) {
    return AlertItem(
      id: json['id'] as int? ?? 0,
      alertType: (json['alert_type'] ?? 'general').toString(),
      title: (json['title'] ?? 'Thông báo').toString(),
      message: (json['message'] ?? '').toString(),
      severity: (json['severity'] ?? 'INFO').toString(),
      imageBase64: json['image_base64'] as String?,
      imagePath: json['image_path'] as String?,
      trackId: json['track_id'] as int?,
      isRead: json['is_read'] == true,
      isResolved: json['is_resolved'] == true,
      createdAt: json['created_at'] != null
          ? DateTime.tryParse(json['created_at'].toString()) ?? DateTime.now()
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'alert_type': alertType,
        'title': title,
        'message': message,
        'severity': severity,
        'image_base64': imageBase64,
        'image_path': imagePath,
        'track_id': trackId,
        'is_read': isRead,
        'is_resolved': isResolved,
        'created_at': createdAt.toIso8601String(),
      };
}

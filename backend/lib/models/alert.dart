class Alert {
  final int id;
  final String alertType;
  final String title;
  final String message;
  final String severity; // CRITICAL, WARNING, INFO
  final String? imageBase64;
  final String? imagePath;
  final int? trackId;
  final bool isRead;
  final bool isResolved;
  final DateTime createdAt;

  Alert({
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

  factory Alert.fromMap(Map<String, dynamic> map) {
    return Alert(
      id: map['id'] as int,
      alertType: map['alert_type'] as String,
      title: map['title'] as String,
      message: (map['message'] ?? '') as String,
      severity: (map['severity'] ?? 'INFO') as String,
      imageBase64: map['image_base64'] as String?,
      imagePath: map['image_path'] as String?,
      trackId: map['track_id'] as int?,
      isRead: (map['is_read'] ?? false) as bool,
      isResolved: (map['is_resolved'] ?? false) as bool,
      createdAt: map['created_at'] is DateTime
          ? map['created_at'] as DateTime
          : DateTime.parse(map['created_at'].toString()),
    );
  }
}

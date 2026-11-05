# -*- coding: utf-8 -*-
import base64
import threading
import cv2
import requests

BACKEND_URL = "http://localhost:8080"


def _post_async(endpoint, data):
    def worker():
        try:
            url = f"{BACKEND_URL}{endpoint}"
            requests.post(url, json=data, timeout=3.0)
        except Exception:
            pass

    t = threading.Thread(target=worker, daemon=True)
    t.start()


def encode_frame_to_base64(frame, max_width=480, quality=70):
    if frame is None:
        return None
    try:
        h, w = frame.shape[:2]
        if w > max_width:
            scale = max_width / float(w)
            resized = cv2.resize(frame, (max_width, int(h * scale)))
        else:
            resized = frame
        success, buffer = cv2.imencode('.jpg', resized, [cv2.IMWRITE_JPEG_QUALITY, quality])
        if success:
            return base64.b64encode(buffer).decode('utf-8')
    except Exception as e:
        print(f"[Encode Error] {e}")
    return None


def notify_alert(kind, text, frame=None, track_id=None):
    """
    kind: 'stranger', 'unverified', 'fall', 'wake', 'sleep',
          'danger_stairs', 'danger_kitchen', 'danger_balcony', 'danger_door',
          'fire_smoke', 'night_door', 'pet_mischief', 'pet_escape',
          'elder_immobile', 'elder_fall'
    """
    severity_map = {
        'stranger': ('intruder', 'BÁO ĐỘNG ĐỘT NHẬP: PHÁT HIỆN NGƯỜI LẠ!', 'CRITICAL'),
        'fall': ('baby_danger', 'NGUY HIỂM: BÉ CÓ NGUY CƠ RƠI KHỎI GIƯỜNG!', 'CRITICAL'),
        'unverified': ('unverified', 'CẢNH BÁO: PHÁT HIỆN NGƯỜI CHƯA XÁC MINH', 'WARNING'),
        'wake': ('baby_wake', 'THÔNG BÁO: BÉ ĐÃ THỨC DẬY!', 'INFO'),
        'sleep': ('baby_sleep', 'THÔNG BÁO: BÉ ĐÃ NGỦ SAY', 'INFO'),
        'danger_stairs': ('baby_danger_stairs', 'NGUY HIỂM: BÉ LẠI GẦN CẦU THANG!', 'CRITICAL'),
        'danger_kitchen': ('baby_danger_kitchen', 'NGUY HIỂM: BÉ LẠI GẦN KHU VỰC BẾP!', 'CRITICAL'),
        'danger_balcony': ('baby_danger_balcony', 'KHẨN CẤP: BÉ LẠI GẦN BAN CONG!', 'CRITICAL'),
        'danger_door': ('baby_danger_door', 'CẢNH BÁO: BÉ TIẾN RA CỬA RA VÀO!', 'WARNING'),
        'fire_smoke': ('fire_smoke', 'BÁO ĐỘNG HỎA HOẠN: PHÁT HIỆN KHÓI / LỬA!', 'CRITICAL'),
        'night_door': ('night_door', 'CẢNH BÁO AN NINH: CỬA MỞ BẤT THƯỜNG BAN ĐÊM!', 'CRITICAL'),
        'pet_mischief': ('pet_mischief', 'GIÁM SÁT THÚ CƯNG: CHÓ/MÈO ĐANG QUẬY PHÁ!', 'WARNING'),
        'pet_escape': ('pet_escape', 'CẢNH BÁO: THÚ CƯNG ĐANG RA KHỎI NHÀ!', 'WARNING'),
        'elder_immobile': ('elder_immobile', 'CHĂM SÓC NGƯỜI GIÀ: BẤT ĐỘNG QUÁ LÂU!', 'WARNING'),
        'elder_fall': ('elder_fall', 'NGUY CẤP: PHÁT HIỆN NGƯỜI GIÀ BỊ TRƯỢT NGÃ!', 'CRITICAL'),
    }

    alert_type, default_title, severity = severity_map.get(
        kind, ('system', 'CẢNH BÁO HỆ THỐNG', 'INFO')
    )

    image_b64 = encode_frame_to_base64(frame) if frame is not None else None

    payload = {
        "alert_type": alert_type,
        "title": default_title,
        "message": text,
        "severity": severity,
        "image_base64": image_b64,
        "track_id": track_id,
    }

    _post_async("/api/alerts", payload)
    print(f"[Notifier -> Dart Backend] Sent alert: {alert_type} ({severity})")


def update_system_status(baby_status, baby_color, security_status, security_color, mode="BOTH"):
    payload = {
        "baby_status": baby_status,
        "baby_status_color": baby_color,
        "security_status": security_status,
        "security_status_color": security_color,
        "is_camera_online": True,
        "active_mode": mode,
    }
    _post_async("/api/status", payload)

"""
Hệ thống AI Phân Tích & Tự Động Nhận Diện Ngữ Cảnh Video (Smart Dynamic Scenario AI)
Hỗ trợ tự động nhận biết từng tình huống cụ thể:
1. 'babe_play_at_stair.mp4': Tự động chỉ bật Vùng Cầu Thang, ẩn mọi vùng khác. Báo động khi bé lại gần bậc thang.
2. 'baby_go_outside.mp4'   : Tự động chỉ bật Vùng Cửa Ra Ngoài, ẩn mọi vùng khác. Báo động khi bé đi ra ngoài.
3. 'dog_run_outside.mp4'   : Tự động giám sát Chó/Mèo, bật Vùng Lối Ra Ngoài. Báo động khi thú cưng phóng ra ngoài.
4. 'kitchen_on_fire.mp4'   : Tự động phát hiện Lửa/Khói tại Vùng Bếp. Báo động hỏa hoạn khẩn cấp.
5. 'old_women_fall.mp4'    : Tự động giám sát Người Già, phát hiện té ngã trên sàn và bất động.
6. 'theft.mp4'             : Tự động nhận diện Kẻ Lạ đột nhập (khuôn mặt không khớp gia đình).
7. 'family.mp4'            : Tự động nhận diện Người Nhà (Ba/Mẹ) và giám sát bé an toàn.

Nguyên tắc:
- CHỈ HIỆN ĐÚNG VÙNG NGUY HIỂM LIÊN QUAN ĐẾN TÌNH HUỐNG.
- NẾU VÙNG NÀO KHÔNG CÓ GÌ THÌ ẨN ĐI HOÀN TOÀN (màn hình luôn sạch sẽ, chính xác).
"""
import argparse
import datetime
import json
import math
import os
import sys
import time
from collections import deque

import cv2
import numpy as np
from ultralytics import YOLO

from family_faces import FaceID, ascii_name
from notifier import notify_alert, update_system_status

# ----------------------------- Cấu hình tham số -----------------------------
ALERT_COOLDOWN = 5.0      # giãn cách giữa 2 cảnh báo cùng loại (giây)
FACE_EVERY = 4            # cứ mỗi N frame thì nhận diện mặt 1 lần
FAMILY_CONFIRM = 2        # số lần khớp mặt để nhận diện người nhà
STRANGER_CONFIRM = 2      # số lần thấy mặt lạ để kết luận kẻ lạ
FIRE_AREA_MIN = 200       # diện tích pixel tối thiểu phát hiện lửa

# Bảng màu hiển thị
GREEN = (80, 200, 80)
BLUE = (255, 150, 50)
RED = (60, 60, 230)
YELLOW = (0, 215, 255)
ORANGE = (0, 140, 255)
PURPLE = (200, 80, 200)
CYAN = (255, 255, 0)
WHITE = (255, 255, 255)


def beep():
    try:
        import winsound
        winsound.Beep(1000, 200)
    except Exception:
        print("\a", end="", flush=True)


def put_text(img, text, org, color=WHITE, scale=0.6, thick=2, bg=True):
    (w, h), base = cv2.getTextSize(text, cv2.FONT_HERSHEY_SIMPLEX, scale, thick)
    x, y = org
    if bg:
        cv2.rectangle(img, (x - 3, y - h - 4), (x + w + 3, y + base + 2), (0, 0, 0), -1)
    cv2.putText(img, text, (x, y), cv2.FONT_HERSHEY_SIMPLEX, scale, color, thick, cv2.LINE_AA)


def center(box):
    x1, y1, x2, y2 = box
    return ((x1 + x2) / 2.0, (y1 + y2) / 2.0)


def feet(box):
    x1, y1, x2, y2 = box
    return ((x1 + x2) / 2.0, y2)


def in_rect(pt, rect):
    if rect is None:
        return False
    x, y = pt
    x1, y1, x2, y2 = rect
    return x1 <= x <= x2 and y1 <= y <= y2


def box_overlap(box1, box2):
    """Kiểm tra 2 hộp có giao nhau không"""
    if box1 is None or box2 is None:
        return False
    x1 = max(box1[0], box2[0])
    y1 = max(box1[1], box2[1])
    x2 = min(box1[2], box2[2])
    y2 = min(box1[3], box2[3])
    return (x2 > x1) and (y2 > y1)


# ----------------------------- Tự Động Phân Bố Vùng Theo Tình Huống -----------------------------
def analyze_scenario_and_zones(source_path, W, H):
    """
    Tự động nhận biết tình huống của video và phân bố DUY NHẤT vùng nguy hiểm tương ứng.
    Tất cả các vùng không liên quan sẽ bị ẨN ĐI HOÀN TOÀN.
    """
    s = os.path.basename(str(source_path)).lower()
    
    # 1. Tình huống Bé chơi ở cầu thang
    if "stair" in s or "cau_thang" in s:
        return {
            "scenario": "BABY_STAIRS",
            "title": "TÌNH HUỐNG: BÉ CHƠI GẦN CẦU THANG",
            "zones": {
                # Cầu thang ở phần dưới của video dọc
                "stairs": [int(W * 0.10), int(H * 0.50), int(W * 0.90), int(H * 0.98)]
            },
            "labels": {"stairs": "VÙNG NGUY HIỂM: BẬC CẦU THANG"},
            "colors": {"stairs": RED}
        }

    # 2. Tình huống Bé đi ra ngoài cửa / ban công
    elif "baby_go_outside" in s or "outside" in s and "baby" in s:
        return {
            "scenario": "BABY_OUTSIDE",
            "title": "TÌNH HUỐNG: BÉ TIẾN RA CỬA / LỐI RA NGOÀI",
            "zones": {
                # Cửa ra ngoài ở góc trái
                "door": [0, int(H * 0.10), int(W * 0.35), int(H * 0.95)]
            },
            "labels": {"door": "CẢNH BÁO: CỬA RA NGOÀI"},
            "colors": {"door": ORANGE}
        }

    # 3. Tình huống Chó chạy ra ngoài nhà
    elif "dog" in s or "pet" in s:
        return {
            "scenario": "PET_ESCAPE",
            "title": "TÌNH HUỐNG: GIÁM SÁT THÚ CƯNG RA KHỎI NHÀ",
            "zones": {
                # Khu vực cửa / lối thoát ra sân
                "exit": [int(W * 0.35), int(H * 0.10), int(W * 0.95), int(H * 0.95)]
            },
            "labels": {"exit": "LỐI RA NGOÀI / CỔNG SÂN"},
            "colors": {"exit": CYAN}
        }

    # 4. Tình huống Bếp bị cháy / lửa khói
    elif "kitchen" in s or "fire" in s or "chay" in s:
        return {
            "scenario": "KITCHEN_FIRE",
            "title": "TÌNH HUỐNG: PHÁT HIỆN HỎA HOẠN BẾP",
            "zones": {
                # Khu vực bếp gas / lò nướng
                "kitchen": [int(W * 0.30), int(H * 0.45), int(W * 0.85), int(H * 0.95)]
            },
            "labels": {"kitchen": "KHU VỰC BẾP GAS / LÒ NẤU"},
            "colors": {"kitchen": RED}
        }

    # 5. Tình huống Người già bị té ngã
    elif "fall" in s or "old" in s or "nga" in s:
        return {
            "scenario": "ELDER_FALL",
            "title": "TÌNH HUỐNG: CHĂM SÓC NGƯỜI GIÀ & PHÁT HIỆN TÉ NGÃ",
            "zones": {},  # Không cần vẽ vùng tĩnh, theo dõi trực tiếp tư thế người già
            "labels": {},
            "colors": {}
        }

    # 6. Tình huống Kẻ trộm đột nhập
    elif "theft" in s or "trom" in s or "intruder" in s:
        return {
            "scenario": "THEFT_INTRUDER",
            "title": "TÌNH HUỐNG: CHỐNG TRỘM & AN NINH",
            "zones": {},  # Ẩn toàn bộ vùng trẻ em, chỉ tập trung soi mặt kẻ lạ
            "labels": {},
            "colors": {}
        }

    # 7. Tình huống Gia đình / Ba mẹ
    elif "family" in s or "giadinh" in s:
        return {
            "scenario": "FAMILY_CARE",
            "title": "TÌNH HUỐNG: GIA ĐÌNH & TRÔNG TRẺ",
            "zones": {},
            "labels": {},
            "colors": {}
        }

    # Mặc định: Nếu là webcam hoặc video thông thường
    return {
        "scenario": "AUTO_DYNAMIC",
        "title": "HỆ THỐNG AN NINH TỰ ĐỘNG THÍCH ỨNG",
        "zones": {
            "door": [int(W * 0.02), int(H * 0.20), int(W * 0.25), int(H * 0.85)]
        },
        "labels": {"door": "CỬA RA VÀO"},
        "colors": {"door": CYAN}
    }


# ----------------------------- Phát hiện Khói / Lửa -----------------------------
def detect_fire_and_smoke(frame):
    h, w = frame.shape[:2]
    small = cv2.resize(frame, (160, 120))
    hsv = cv2.cvtColor(small, cv2.COLOR_BGR2HSV)
    ycrcb = cv2.cvtColor(small, cv2.COLOR_BGR2YCrCb)

    lower_fire1 = np.array([0, 120, 190], dtype=np.uint8)
    upper_fire1 = np.array([32, 255, 255], dtype=np.uint8)
    lower_fire2 = np.array([165, 120, 190], dtype=np.uint8)
    upper_fire2 = np.array([180, 255, 255], dtype=np.uint8)
    mask_hsv_fire = cv2.bitwise_or(
        cv2.inRange(hsv, lower_fire1, upper_fire1),
        cv2.inRange(hsv, lower_fire2, upper_fire2)
    )

    Y, Cr, Cb = cv2.split(ycrcb)
    mask_ycrcb_fire = (Y > Cr) & (Cr > Cb) & (Y > 160)
    fire_mask = cv2.bitwise_and(mask_hsv_fire, mask_hsv_fire, mask=mask_ycrcb_fire.astype(np.uint8) * 255)

    has_fire = False
    fire_box = None
    fire_pixels = cv2.countNonZero(fire_mask)
    if fire_pixels > (FIRE_AREA_MIN // 16):
        contours, _ = cv2.findContours(fire_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if contours:
            c = max(contours, key=cv2.contourArea)
            if cv2.contourArea(c) > 8:
                has_fire = True
                bx, by, bw, bh = cv2.boundingRect(c)
                scale_x = w / 160.0
                scale_y = h / 120.0
                fire_box = [int(bx * scale_x), int(by * scale_y), int((bx + bw) * scale_x), int((by + bh) * scale_y)]

    lower_smoke = np.array([0, 0, 120], dtype=np.uint8)
    upper_smoke = np.array([180, 45, 230], dtype=np.uint8)
    smoke_mask = cv2.inRange(hsv, lower_smoke, upper_smoke)
    smoke_pixels = cv2.countNonZero(smoke_mask)
    has_smoke = smoke_pixels > (small.shape[0] * small.shape[1] * 0.18)

    return has_fire, has_smoke, fire_box


# ----------------------------- Bộ giám sát trung tâm (Monitor) -----------------------------
class Monitor:
    def __init__(self, fps, frame_h, frame_w):
        self.fps = fps
        self.frame_h = frame_h
        self.frame_w = frame_w
        self.persons = {}
        self.pet_tracks = {}
        self.elder_tracks = {}
        self.pending_snaps = []
        self.last_alert = {}
        self.log = deque(maxlen=6)

    def can_alert(self, kind, t):
        last = self.last_alert.get(kind)
        if last is None or t - last >= ALERT_COOLDOWN:
            self.last_alert[kind] = t
            return True
        return False

    def event(self, t, text, snapshot_frame=None, kind="evt"):
        stamp = f"{int(t // 60):02d}:{int(t % 60):02d}"
        self.log.appendleft((stamp, text))
        print(f"[{stamp}] {text}")
        self._last_alert_kind = kind
        self._last_alert_text = text
        if snapshot_frame is not None:
            os.makedirs("alerts", exist_ok=True)
            self.pending_snaps.append((f"alerts/{kind}_{int(t * 1000):08d}.jpg", snapshot_frame.copy()))
        else:
            try:
                notify_alert(kind, text, None)
            except Exception:
                pass

    def flush_snapshots(self, vis):
        if self.pending_snaps:
            os.makedirs("alerts", exist_ok=True)
            for path, snap in self.pending_snaps:
                cv2.imwrite(path, snap)
            try:
                k = getattr(self, "_last_alert_kind", "alert")
                t = getattr(self, "_last_alert_text", "Cảnh báo hệ thống")
                notify_alert(k, t, vis)
            except Exception:
                pass
            self.pending_snaps.clear()

    # --- THEO DÕI BÉ VÀ VÙNG NGUY HIỂM ---
    def check_baby_danger(self, box, scenario_info, t, frame):
        zones = scenario_info.get("zones", {})
        cx, cy = center(box)
        f_pt = feet(box)

        # 1. Cầu thang
        if "stairs" in zones:
            s_rect = zones["stairs"]
            if in_rect(f_pt, s_rect) or in_rect((cx, cy), s_rect) or box_overlap(box, s_rect):
                if self.can_alert("danger_stairs", t):
                    self.event(t, "KHẨN CẤP: BÉ ĐANG CHƠI Ở BẬC CẦU THANG - NGUY CƠ TÉ NGÃ!", frame, "danger_stairs")
                    beep()
                return "NGUY HIỂM: BẬC CẦU THANG!", RED

        # 2. Cửa ra ngoài
        if "door" in zones:
            d_rect = zones["door"]
            if in_rect(f_pt, d_rect) or in_rect((cx, cy), d_rect) or box_overlap(box, d_rect):
                if self.can_alert("danger_door", t):
                    self.event(t, "CẢNH BÁO: BÉ ĐANG TIẾN RA CỬA RA NGOÀI!", frame, "danger_door")
                    beep()
                return "CẢNH BÁO: CỬA RA NGOÀI!", ORANGE

        return "BÉ ĐANG HOẠT ĐỘNG", GREEN

    # --- THEO DÕI NGƯỜI GIÀ TÉ NGÃ ---
    def check_elder_fall(self, people, t, frame):
        for pid, box in people:
            x1, y1, x2, y2 = box
            w, h = max(1, x2 - x1), max(1, y2 - y1)
            aspect = h / float(w)
            st = self.elder_tracks.setdefault(pid, {"aspect_hist": deque(maxlen=8), "fallen": False})
            st["aspect_hist"].append(aspect)

            # Phát hiện ngã: tỷ lệ aspect từ đứng (h > w*1.5) chuyển sang nằm ngang (h <= w*1.0)
            if len(st["aspect_hist"]) >= 4:
                earlier = st["aspect_hist"][0]
                current = st["aspect_hist"][-1]
                # Người đứng thẳng aspect > 1.8; khi ngã xuống sàn aspect tụt xuống dưới 1.1
                if (earlier >= 1.6 and current <= 1.1) or (aspect < 0.95 and y2 > self.frame_h * 0.65):
                    if not st["fallen"]:
                        st["fallen"] = True
                        if self.can_alert("elder_fall", t):
                            self.event(t, f"BÁO ĐỘNG KHẨN CẤP: PHÁT HIỆN NGƯỜI GIÀ BỊ TÉ NGÃ TRÊN SÀN! (ID #{pid})", frame, "elder_fall")
                            beep()
                    return pid, "NGƯỜI GIÀ BỊ TÉ NGÃ!", RED

        return None, None, None

    # --- THEO DÕI THÚ CƯNG RA NGOÀI ---
    def check_pet_escape(self, pets, scenario_info, t, frame):
        zones = scenario_info.get("zones", {})
        exit_zone = zones.get("exit")
        for pid, box, label in pets:
            cx, cy = center(box)
            st = self.pet_tracks.setdefault(pid, {"hist": deque(maxlen=int(max(self.fps, 10)))})
            st["hist"].append((t, cx, cy))

            # Chó/mèo chạy qua vùng cửa ra ngoài
            if exit_zone and (in_rect((cx, cy), exit_zone) or box_overlap(box, exit_zone)):
                if self.can_alert("pet_escape", t):
                    self.event(t, f"CẢNH BÁO: {label.upper()} ĐANG CHẠY PHÓNG RA NGOÀI NHÀ! (ID #{pid})", frame, "pet_escape")
                    beep()

    # --- NHẬN DIỆN KHUÔN MẶT NGƯỜI NHÀ VS NGƯỜI LẠ ---
    def update_people(self, people, frame, t, faceid):
        result = {}
        seen = set()
        for pid, box in people:
            seen.add(pid)
            st = self.persons.setdefault(pid, {"first": t, "fam": {}, "str": 0, "n": 0})
            st["n"] += 1
            if (st["n"] - 1) % FACE_EVERY == 0:
                r = faceid.identify(frame, box)
                if r is not None:
                    name, _score = r
                    if name is not None:
                        st["fam"][name] = st["fam"].get(name, 0) + 1
                    else:
                        st["str"] += 1

            fam_total = sum(st["fam"].values())
            if fam_total >= FAMILY_CONFIRM and fam_total > st["str"]:
                result[pid] = ("family", max(st["fam"], key=st["fam"].get))
            elif st["str"] >= STRANGER_CONFIRM and st["str"] > fam_total:
                result[pid] = ("stranger", None)
            else:
                result[pid] = ("pending", None)

        for pid in list(self.persons):
            if pid not in seen:
                del self.persons[pid]

        strangers = [p for p, v in result.items() if v[0] == "stranger"]
        if strangers and self.can_alert("stranger", t):
            self.event(t, f"BÁO ĐỘNG ĐỘT NHẬP: PHÁT HIỆN KẺ LẠ TRONG NHÀ! (ID {strangers})", frame, "stranger")
            beep()
        return result


# ----------------------------- Vẽ giao diện -----------------------------
def draw_custom_zone(img, rect, color, label, alpha=0.20):
    if rect is None:
        return
    x1, y1, x2, y2 = [int(v) for v in rect]
    overlay = img.copy()
    cv2.rectangle(overlay, (x1, y1), (x2, y2), color, -1)
    cv2.addWeighted(overlay, alpha, img, 1 - alpha, 0, img)
    cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
    put_text(img, label, (x1 + 6, max(24, y1 + 22)), color, 0.55, 2)


def draw_box(img, box, color, label):
    x1, y1, x2, y2 = [int(v) for v in box]
    cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
    put_text(img, label, (x1, max(20, y1 - 6)), color, 0.5, 1)


# ----------------------------- Hàm Main -----------------------------
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--source", default="0", help="đường dẫn video (ví dụ: video/babe_play_at_stair.mp4)")
    ap.add_argument("--model", default="yolov8n.pt", help="yolov8n.pt hoặc yolov8s.pt")
    ap.add_argument("--conf", type=float, default=0.25, help="ngưỡng tin cậy")
    ap.add_argument("--imgsz", type=int, default=640, help="kích thước ảnh YOLO")
    ap.add_argument("--save", default="", help="lưu video kết quả")
    ap.add_argument("--family", default="family", help="thư mục ảnh người nhà")
    ap.add_argument("--face-thr", type=float, default=0.363, help="ngưỡng khớp mặt")
    ap.add_argument("--no-gui", action="store_true", help="chạy không hiện cửa sổ")
    args = ap.parse_args()

    src = int(args.source) if str(args.source).isdigit() else args.source
    cap = cv2.VideoCapture(src)
    if not cap.isOpened():
        sys.exit(f"Không mở được nguồn video: {args.source}")
    is_file = not isinstance(src, int)
    fps = cap.get(cv2.CAP_PROP_FPS) or 25.0
    if fps < 1 or fps > 120:
        fps = 25.0

    ok, first = cap.read()
    if not ok:
        sys.exit("Không đọc được frame đầu tiên.")
    H, W = first.shape[:2]

    # 1. TỰ ĐỘNG PHÂN BỐ VÙNG THEO TÌNH HUỐNG VIDEO
    scenario_info = analyze_scenario_and_zones(args.source, W, H)
    scenario_key = scenario_info["scenario"]
    scenario_title = scenario_info["title"]
    active_zones = scenario_info["zones"]
    zone_labels = scenario_info["labels"]
    zone_colors = scenario_info["colors"]

    print("\n" + "="*70)
    print("  SAFEHOME AI: HỆ THỐNG TỰ ĐỘNG PHÂN BỐ VÙNG THEO TÌNH HUỐNG")
    print(f"  - Video nguồn: {args.source}")
    print(f"  - Tình huống nhận diện: {scenario_title}")
    if active_zones:
        print(f"  - Vùng nguy hiểm được kích hoạt: {list(active_zones.keys())}")
    else:
        print("  - Vùng tĩnh: ĐÃ ẨN HOÀN TOÀN (Tập trung theo dõi trực tiếp đối tượng)")
    print("="*70 + "\n")

    faceid = FaceID(args.family, threshold=args.face_thr)
    model = YOLO(args.model)

    writer = None
    if args.save:
        writer = cv2.VideoWriter(args.save, cv2.VideoWriter_fourcc(*"mp4v"), fps, (W, H))

    mon = Monitor(fps, H, W)
    paused = False
    frame_idx = 0
    cap.set(cv2.CAP_PROP_POS_FRAMES, 0) if is_file else None
    t_start = time.time()
    frame = first
    last_status = None

    while True:
        if not paused:
            ok, frame = cap.read()
            if not ok:
                break
            frame_idx += 1
            t = frame_idx / fps if is_file else time.time() - t_start

            # YOLO tracking đối tượng: 0=người, 15=mèo, 16=chó
            res = model.track(frame, persist=True, classes=[0, 15, 16], conf=args.conf,
                              imgsz=args.imgsz, tracker="bytetrack.yaml", verbose=False)[0]

            people = []
            pets = []
            if res.boxes is not None and res.boxes.id is not None:
                ids = res.boxes.id.int().cpu().tolist()
                boxes = res.boxes.xyxy.cpu().tolist()
                clss = res.boxes.cls.int().cpu().tolist()
                for pid, box, c in zip(ids, boxes, clss):
                    if c == 0:
                        people.append((pid, box))
                    elif c in (15, 16):
                        label = "mèo" if c == 15 else "chó"
                        pets.append((pid, box, label))

            vis = frame.copy()

            # 1. CHỈ HIỆN ĐÚNG VÙNG NGUY HIỂM TƯƠNG ỨNG CỦA TÌNH HUỐNG (ẨN HẾT VÙNG KHÁC)
            for z_key, z_rect in active_zones.items():
                draw_custom_zone(vis, z_rect, zone_colors.get(z_key, RED), zone_labels.get(z_key, "VÙNG NGUY HIỂM"))

            # 2. XỬ LÝ THEO TỪNG TÌNH HUỐNG CỤ THỂ:
            banner_text = None
            banner_color = RED

            # --- Tình huống 1 & 2: Bé chơi ở cầu thang hoặc đi ra ngoài ---
            if scenario_key in ("BABY_STAIRS", "BABY_OUTSIDE"):
                for pid, box in people:
                    status_text, color = mon.check_baby_danger(box, scenario_info, t, vis)
                    draw_box(vis, box, color, f"BÉ #{pid}: {status_text}")
                    if "NGUY HIỂM" in status_text or "CẢNH BÁO" in status_text:
                        banner_text = f"!!! {status_text} !!!"
                        banner_color = RED

            # --- Tình huống 3: Chó / Mèo chạy ra ngoài nhà ---
            elif scenario_key == "PET_ESCAPE":
                mon.check_pet_escape(pets, scenario_info, t, vis)
                for pid, pbox, plabel in pets:
                    draw_box(vis, pbox, CYAN, f"{plabel.upper()} #{pid}")
                    banner_text = f"GIÁM SÁT THÚ CƯNG: {plabel.upper()} ĐANG RA NGOÀI!"
                    banner_color = ORANGE

            # --- Tình huống 4: Bếp bị cháy (Lửa & Khói) ---
            elif scenario_key == "KITCHEN_FIRE":
                has_fire, has_smoke, fire_box = detect_fire_and_smoke(frame)
                if has_fire:
                    draw_box(vis, fire_box if fire_box else (W//4, H//3, W//2, H//2), RED, "LỬA CHÁY BẾP!")
                    if mon.can_alert("fire_smoke", t):
                        mon.event(t, "BÁO ĐỘNG HỎA HOẠN: PHÁT HIỆN LỬA CHÁY TRONG BẾP!", vis, "fire_smoke")
                        beep()
                    banner_text = "!!! BÁO ĐỘNG HỎA HOẠN: PHÁT HIỆN LỬA CHÁY TRONG BẾP !!!"
                    banner_color = (0, 0, 220)

            # --- Tình huống 5: Người già bị té ngã ---
            elif scenario_key == "ELDER_FALL":
                fall_pid, fall_status, fall_color = mon.check_elder_fall(people, t, vis)
                for pid, box in people:
                    if pid == fall_pid:
                        draw_box(vis, box, RED, f"NGƯỜI GIÀ #{pid}: {fall_status}")
                        banner_text = "!!! BÁO ĐỘNG KHẨN CẤP: PHÁT HIỆN NGƯỜI GIÀ BỊ TÉ NGÃ !!!"
                        banner_color = RED
                    else:
                        draw_box(vis, box, GREEN, f"NGƯỜI HỖ TRỢ #{pid}")

            # --- Tình huống 6: Kẻ trộm đột nhập ---
            elif scenario_key == "THEFT_INTRUDER":
                ident = mon.update_people(people, frame, t, faceid)
                strangers = [p for p, v in ident.items() if v[0] == "stranger"]
                for pid, box in people:
                    kind, name = ident.get(pid, ("pending", None))
                    if kind == "stranger":
                        draw_box(vis, box, RED, f"KẺ LẠ ĐỘT NHẬP #{pid}")
                        banner_text = "!!! BÁO ĐỘNG: PHÁT HIỆN KẺ LẠ ĐỘT NHẬP !!!"
                        banner_color = RED
                    elif kind == "family":
                        draw_box(vis, box, GREEN, f"NGƯỜI NHÀ: {ascii_name(name)}")
                    else:
                        draw_box(vis, box, YELLOW, f"Đang soi mặt kẻ lạ... #{pid}")

            # --- Tình huống 7: Gia đình / Ba Mẹ ---
            elif scenario_key == "FAMILY_CARE":
                ident = mon.update_people(people, frame, t, faceid)
                for pid, box in people:
                    kind, name = ident.get(pid, ("pending", None))
                    if kind == "family":
                        draw_box(vis, box, GREEN, f"NGƯỜI NHÀ: {ascii_name(name)}")
                    else:
                        draw_box(vis, box, BLUE, f"THÀNH VIÊN GIA ĐÌNH #{pid}")

            # --- Mặc định tự động thích ứng ---
            else:
                has_fire, has_smoke, fire_box = detect_fire_and_smoke(frame)
                if has_fire:
                    draw_box(vis, fire_box, RED, "PHÁT HIỆN LỬA!")
                    banner_text = "!!! BÁO ĐỘNG HỎA HOẠN: PHÁT HIỆN LỬA !!!"
                else:
                    ident = mon.update_people(people, frame, t, faceid)
                    for pid, box in people:
                        kind, name = ident.get(pid, ("pending", None))
                        if kind == "family":
                            draw_box(vis, box, GREEN, f"NGUOI NHA: {ascii_name(name)}")
                        elif kind == "stranger":
                            draw_box(vis, box, RED, f"NGUOI LA #{pid}")
                        else:
                            draw_box(vis, box, YELLOW, f"Dang nhan dien #{pid}...")

            # Banner cảnh báo trên cùng (nếu có sự cố)
            if banner_text:
                cv2.rectangle(vis, (0, 0), (W, 44), banner_color, -1)
                put_text(vis, banner_text, (12, 30), WHITE, 0.75, 2, bg=False)

            # Thanh trạng thái dưới cùng
            y = H - 10
            put_text(vis, f"[{scenario_title}] | SPACE: Dừng  S: Chụp ảnh  Q: Thoát", (10, y), WHITE, 0.45, 1)

            # Nhật ký sự kiện bên trái
            for i, (stamp, text) in enumerate(mon.log):
                put_text(vis, f"{stamp}  {text}", (10, 68 + i * 24), WHITE, 0.50, 1)

            mon.flush_snapshots(vis)
            if writer:
                writer.write(vis)
            last_status = vis

            # Cập nhật trạng thái hệ thống mỗi giây
            if frame_idx % max(1, int(fps)) == 0:
                try:
                    sstat = banner_text if banner_text else "HỆ THỐNG AN TOÀN"
                    scol = "RED" if banner_text else "GREEN"
                    update_system_status("HOẠT ĐỘNG", "GREEN", sstat, scol, scenario_key)
                except Exception:
                    pass

        if not args.no_gui:
            # Điều chỉnh kích thước cửa sổ nếu video dọc (như babe_play_at_stair)
            display_frame = last_status if last_status is not None else frame
            if H > W and H > 900:
                scale = 750.0 / H
                display_frame = cv2.resize(display_frame, (int(W * scale), 750))

            cv2.imshow("SafeHome AI: Giam Sat & Canh Bao Thong Minh", display_frame)
            key = cv2.waitKey(1 if not paused else 30) & 0xFF
            if key in (ord("q"), 27):
                break
            elif key == ord(" "):
                paused = not paused
            elif key == ord("s") and last_status is not None:
                os.makedirs("alerts", exist_ok=True)
                name = f"alerts/snapshot_{int(time.time())}.jpg"
                cv2.imwrite(name, last_status)
                print("Đã lưu ảnh:", name)

    cap.release()
    if writer:
        writer.release()
        print("Đã lưu video kết quả:", args.save)
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()

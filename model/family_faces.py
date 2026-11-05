"""
Nhận diện mặt người nhà bằng OpenCV (YuNet phát hiện mặt + SFace nhận diện).
Không cần cài thêm thư viện nào ngoài opencv-python. Model ONNX (~40MB) tự tải lần đầu.

Cấu trúc thư mục ảnh người nhà:
    family/
        Ba/    anh1.jpg  anh2.jpg ...
        Me/    anh1.jpg  anh2.jpg ...
Mỗi người nên có 3-5 ảnh rõ mặt (nhìn thẳng, hơi nghiêng, sáng khác nhau).
"""
import os
import sys
import unicodedata
import urllib.request

import cv2
import numpy as np

MODEL_DIR = "models"
DET_NAME = "face_detection_yunet_2023mar.onnx"
REC_NAME = "face_recognition_sface_2021dec.onnx"
BASE = "https://github.com/opencv/opencv_zoo/raw/main/models/"
DET_URL = BASE + "face_detection_yunet/" + DET_NAME
REC_URL = BASE + "face_recognition_sface/" + REC_NAME

IMG_EXT = (".jpg", ".jpeg", ".png", ".bmp", ".webp")


def ascii_name(s):
    """Bỏ dấu tiếng Việt để cv2.putText hiển thị được."""
    s = s.replace("đ", "d").replace("Đ", "D")
    return unicodedata.normalize("NFKD", s).encode("ascii", "ignore").decode("ascii")


def _download(url, path):
    if os.path.exists(path) and os.path.getsize(path) > 100_000:
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    print(f"Đang tải {os.path.basename(path)} ...")
    try:
        urllib.request.urlretrieve(url, path)
    except Exception as e:
        if os.path.exists(path):
            os.remove(path)
        sys.exit(f"Không tải được model: {e}\n"
                 f"Hãy tải thủ công file này:\n  {url}\nrồi đặt vào thư mục '{os.path.dirname(path)}/'")


def imread_unicode(path):
    """cv2.imread không đọc được đường dẫn có dấu trên Windows -> dùng imdecode."""
    try:
        data = np.fromfile(path, dtype=np.uint8)
        return cv2.imdecode(data, cv2.IMREAD_COLOR)
    except Exception:
        return None


class FaceID:
    def __init__(self, family_dir="family", threshold=0.363, min_face=24, det_score=0.8):
        """threshold: ngưỡng cosine (0.363 là khuyến nghị của SFace). Cao hơn = khắt khe hơn."""
        det_path = os.path.join(MODEL_DIR, DET_NAME)
        rec_path = os.path.join(MODEL_DIR, REC_NAME)
        _download(DET_URL, det_path)
        _download(REC_URL, rec_path)

        self.threshold = threshold
        self.min_face = min_face
        self.det = cv2.FaceDetectorYN.create(det_path, "", (320, 320), 0.7, 0.3, 5000)
        self.rec = cv2.FaceRecognizerSF.create(rec_path, "")
        self.det_score = det_score
        self.db = {}   # tên -> [embedding, ...]
        self._load_family(family_dir)

    # ---------- nội bộ ----------
    def _detect(self, img):
        h, w = img.shape[:2]
        self.det.setInputSize((w, h))
        _, faces = self.det.detect(img)
        return [] if faces is None else faces

    def _embed(self, img, face_row):
        aligned = self.rec.alignCrop(img, face_row)
        return self.rec.feature(aligned)

    def _load_family(self, family_dir):
        if not os.path.isdir(family_dir):
            os.makedirs(family_dir, exist_ok=True)
        for name in sorted(os.listdir(family_dir)):
            folder = os.path.join(family_dir, name)
            if not os.path.isdir(folder):
                continue
            feats = []
            for fn in sorted(os.listdir(folder)):
                if not fn.lower().endswith(IMG_EXT):
                    continue
                img = imread_unicode(os.path.join(folder, fn))
                if img is None:
                    print(f"  ! Không đọc được ảnh: {name}/{fn}")
                    continue
                if img.shape[1] > 1280:                       # thu nhỏ ảnh quá lớn cho nhanh
                    s = 1280.0 / img.shape[1]
                    img = cv2.resize(img, None, fx=s, fy=s)
                faces = self._detect(img)
                if len(faces) == 0:
                    print(f"  ! Không thấy mặt trong ảnh: {name}/{fn}")
                    continue
                best = max(faces, key=lambda f: f[2] * f[3])  # lấy mặt to nhất
                feats.append(self._embed(img, best))
            if feats:
                self.db[name] = feats
        if self.db:
            names_summary = ", ".join(f"{ascii_name(n)} ({len(f)} anh)" for n, f in self.db.items())
            print(f"Nguoi nha da dang ky: {names_summary}")
        else:
            print(f"CANH BAO: chua co anh nguoi nha trong thu muc '{family_dir}/' "
                  f"-> moi nguoi deu bi coi la NGUOI LA.")

    # ---------- API ----------
    def identify(self, frame, box):
        """
        Nhận diện mặt trong khung người `box` = (x1,y1,x2,y2).
        Trả về:
            None               nếu không thấy mặt / mặt quá nhỏ (không kết luận được)
            (tên, điểm)        nếu khớp người nhà
            (None, điểm)       nếu thấy mặt rõ nhưng KHÔNG khớp ai (người lạ)
        """
        H, W = frame.shape[:2]
        x1, y1, x2, y2 = [int(v) for v in box]
        bh = y2 - y1
        # chỉ lấy phần đầu/ngực (55% phía trên) + lề
        y2h = y1 + int(bh * 0.55)
        mx = int((x2 - x1) * 0.1)
        x1, x2 = max(0, x1 - mx), min(W, x2 + mx)
        y1, y2h = max(0, y1), min(H, y2h)
        if x2 - x1 < 20 or y2h - y1 < 20:
            return None
        crop = frame[y1:y2h, x1:x2]

        scale = 1.0
        if crop.shape[1] < 320:                               # phóng to crop nhỏ để dễ thấy mặt
            scale = min(3.0, 320.0 / crop.shape[1])
            crop = cv2.resize(crop, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

        faces = self._detect(crop)
        if len(faces) == 0:
            return None
        face = max(faces, key=lambda f: f[2] * f[3])
        face_w = face[2] / scale
        face_score = float(face[-1])

        feat = self._embed(crop, face)
        best_name, best = None, -1.0
        for name, feats in self.db.items():
            for f in feats:
                s = float(self.rec.match(feat, f, cv2.FaceRecognizerSF_FR_COSINE))
                if s > best:
                    best, best_name = s, name

        if best_name is not None and best >= self.threshold:
            return best_name, best
        # chỉ kết luận "người lạ" khi mặt đủ to và đủ rõ
        if face_w >= self.min_face and face_score >= self.det_score:
            return None, best
        return None

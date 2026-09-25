import json
import threading
import queue
import tkinter as tk

import vosk
import pyaudio

# ================== CẤU HÌNH ==================
# Đường dẫn tới thư mục model Vosk đã tải và giải nén
# Tải model tại: https://alphacephei.com/vosk/models
# Khuyên dùng bản nhỏ: vosk-model-small-en-us-0.15 (~40MB, tiếng Anh)
MODEL_PATH = "model"  # đổi thành đường dẫn thật, vd: "vosk-model-small-vn-0.4"

SAMPLE_RATE = 16000

# ================== GIAO DIỆN 2D (Tkinter) ==================
root = tk.Tk()
root.title("Voice + AI Demo - Vosk (Offline, Pretrained)")
root.geometry("520x420")
root.configure(bg="#1e1e2f")

canvas = tk.Canvas(root, width=500, height=300, bg="#1e1e2f", highlightthickness=0)
canvas.pack(pady=10)

device_widgets = {}

def draw_device(name, x, y):
    circle = canvas.create_oval(x, y, x + 80, y + 80, fill="#555555", outline="#888888", width=2)
    canvas.create_text(x + 40, y + 100, text=name, fill="white", font=("Arial", 10, "bold"))
    device_widgets[name] = circle

draw_device("Đèn phòng khách", 10, 60)
draw_device("Đèn phòng ngủ", 160, 60)
draw_device("Đèn nhà bếp", 310, 60)
draw_device("Quạt", 220, 190)

status_label = tk.Label(root, text="Đang khởi động Vosk...", bg="#1e1e2f", fg="#00d4ff", font=("Arial", 11))
status_label.pack(pady=5)

recognized_label = tk.Label(root, text="", bg="#1e1e2f", fg="#aaaaaa", font=("Arial", 10))
recognized_label.pack(pady=2)

def update_device_ui(name, state):
    color = "#ffd700" if state else "#555555"
    canvas.itemconfig(device_widgets[name], fill=color)
    status_label.config(text=f"{name}: {'BẬT' if state else 'TẮT'}")

def update_status_text(text):
    status_label.config(text=text)

def update_recognized_text(text):
    recognized_label.config(text=f'Đã nghe: "{text}"')

# ================== XỬ LÝ LỆNH (rule-based, dựa trên văn bản Vosk trả về) ==================
def process_command(text):
    text = text.lower()

    # "bật"/"mở" = BẬT, "tắt" = TẮT
    state = ("bật" in text) or ("mở" in text)

    if "khách" in text:
        root.after(0, update_device_ui, "Đèn phòng khách", state)
    elif "ngủ" in text:
        root.after(0, update_device_ui, "Đèn phòng ngủ", state)
    elif "bếp" in text:
        root.after(0, update_device_ui, "Đèn nhà bếp", state)
    elif "quạt" in text:
        root.after(0, update_device_ui, "Quạt", state)

# ================== PHẦN NHẬN DIỆN GIỌNG NÓI BẰNG VOSK (chạy ở thread riêng) ==================
def voice_loop():
    root.after(0, update_status_text, "Đang tải model Vosk...")
    model = vosk.Model(MODEL_PATH)
    recognizer = vosk.KaldiRecognizer(model, SAMPLE_RATE)

    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paInt16, channels=1, rate=SAMPLE_RATE,
                     input=True, frames_per_buffer=8000)
    stream.start_stream()

    root.after(0, update_status_text, "Đang lắng nghe... (nói: 'bật đèn phòng khách', 'tắt quạt'...)")

    try:
        while True:
            data = stream.read(4000, exception_on_overflow=False)
            if recognizer.AcceptWaveform(data):
                result = json.loads(recognizer.Result())
                text = result.get("text", "")
                if text:
                    root.after(0, update_recognized_text, text)
                    process_command(text)
    finally:
        stream.stop_stream()
        stream.close()
        p.terminate()

# Chạy Vosk ở thread nền, để không làm treo cửa sổ Tkinter
threading.Thread(target=voice_loop, daemon=True).start()

root.mainloop()

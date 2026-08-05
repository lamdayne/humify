#include <SPI.h>
#include <MFRC522.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ====================================================
// 1. CẤU HÌNH CHÂN NỐI PHẦN CỨNG (RC522 + BUZZER)
// ====================================================
#define SS_PIN          15  // SDA cắm D15
#define RST_PIN         22  // RES cắm D22
#define SCK_PIN         18  // SCK cắm D18
#define MISO_PIN        19  // MISO cắm D19
#define MOSI_PIN        23  // MOSI cắm D23

#define BUZZER_PIN      4   // Tín hiệu Còi Buzzer cắm D4

// ====================================================
// 2. CẤU HÌNH WIFI & SERVER API
// ====================================================
const char* WIFI_SSID     = "Tung   quyen";     // Tên WiFi
const char* WIFI_PASSWORD = "777777779";        // Mật khẩu WiFi

const char* API_URL       = "http://192.168.1.18:8080/attendance-logs/nfc-swipe";
const char* IOT_API_KEY   = "IoT-Default-Auth-Key-2026"; 
const char* COMPANY_CODE  = "73f3009b-938b-43a5-aeb2-0c1abbb7cd57"; // Mã công ty của bạn

MFRC522 mfrc522(SS_PIN, RST_PIN);

// Hàm phát tiếng bíp còi (Active HIGH)
void beep(int durationMs) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(durationMs);
  digitalWrite(BUZZER_PIN, LOW);
}

// Báo Chấm công THÀNH CÔNG (Bíp 1 tiếng ngắn 0.15s)
void triggerSuccessNotification() {
  beep(150);
}

// Báo Chấm công THẤT BẠI (Bíp 3 tiếng)
void triggerErrorNotification() {
  beep(100); delay(80);
  beep(100); delay(80);
  beep(300);
}

// Hàm kết nối WiFi
void connectToWiFi() {
  Serial.print("Connecting to WiFi: ");
  Serial.println(WIFI_SSID);
  
  WiFi.disconnect(true);
  delay(500);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi Connected successfully!");
    Serial.print("ESP32 IP Address: ");
    Serial.println(WiFi.localIP());
    beep(80); delay(80); beep(80);
  } else {
    Serial.println("\nWiFi Connection Failed!");
    triggerErrorNotification();
  }
}

// Gửi Request POST lên Backend Spring Boot với mã thẻ ĐỘNG & CompanyCode
void sendAttendanceRequest(String cardUid) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected. Reconnecting...");
    connectToWiFi();
    if (WiFi.status() != WL_CONNECTED) return;
  }

  HTTPClient http;
  http.begin(API_URL);
  
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-IoT-API-Key", IOT_API_KEY);

  // Buffer 300 bytes để chứa vừa mã UUID công ty
  StaticJsonDocument<300> doc;
  doc["cardUid"]      = cardUid;       // Mã UID động vừa quét (VD: "618E5A6E")
  doc["employeeCode"] = cardUid;       // Truyền song song làm mã nhận diện
  doc["companyCode"]  = COMPANY_CODE;  // Mã công ty 73f3009b-938b-43a5-aeb2-0c1abbb7cd57
  doc["deviceInfo"]   = "ESP32-RC522-Gate-01";

  String requestBody;
  serializeJson(doc, requestBody);

  Serial.println("\n------------------------------------");
  Serial.print("Sending POST Request to: ");
  Serial.println(API_URL);
  Serial.print("Payload: ");
  Serial.println(requestBody);

  int httpResponseCode = http.POST(requestBody);

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.printf("HTTP Response Code: %d\n", httpResponseCode);
    Serial.print("Response Payload: ");
    Serial.println(response);

    if (httpResponseCode == 201 || httpResponseCode == 200) { 
      Serial.println(">>> CHAM CONG THANH CONG! <<<");
      triggerSuccessNotification();
    } else { 
      Serial.println(">>> LOI CHAM CONG! <<<");
      triggerErrorNotification();
    }
  } else {
    Serial.printf("Error on sending POST: %s\n", http.errorToString(httpResponseCode).c_str());
    triggerErrorNotification();
  }

  http.end();
  Serial.println("------------------------------------\n");
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(2, OUTPUT);
  digitalWrite(2, LOW);

  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);

  SPI.begin(SCK_PIN, MISO_PIN, MOSI_PIN, SS_PIN);
  mfrc522.PCD_Init();
  Serial.println("RC522 Reader Initialized.");

  connectToWiFi();
}

void loop() {
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }

  // 1. ĐỌC MÃ UID ĐỘNG TỪ THẺ NFC ĐANG QUẸT
  String cardUid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    cardUid += String(mfrc522.uid.uidByte[i] < 0x10 ? "0" : "");
    cardUid += String(mfrc522.uid.uidByte[i], HEX);
  }
  cardUid.toUpperCase();

  Serial.println("\n----------------------------------");
  Serial.print("Phat hien the NFC! UID: ");
  Serial.println(cardUid);

  mfrc522.PICC_HaltA();
  mfrc522.PCD_StopCrypto1();

  // 2. GỬI MÃ THẺ VÀ COMPANY_CODE LÊN SERVER
  sendAttendanceRequest(cardUid);

  delay(2500); // Tạm dừng 2.5s chống quẹt trùng
}

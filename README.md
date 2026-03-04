# 🏢 Efsora Labs Backend API

**Backend API** - Java 21 + Spring Boot 4.0.3 + PostgreSQL + JWT Authentication

---

## 🚀 Hızlı Başlangıç

### 1. PostgreSQL'i Başlat
```bash
docker-compose up -d
```

### 2. Spring Boot'u Çalıştır
```bash
./mvnw spring-boot:run
```

✅ **API Hazır:** http://localhost:8080  
✅ **Swagger UI:** http://localhost:8080/swagger-ui/index.html

---

## 🔌 API Endpoint'leri

### 🔓 **Public Endpoints (Token Gerekmez)**

#### Kullanıcı Kaydı
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ahmet",
    "lastName": "Yılmaz",
    "email": "ahmet@test.com",
    "password": "Pass123!",
    "department": "IT"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet@test.com",
    "password": "Pass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "abc-123-...",
  "email": "ahmet@test.com",
  "firstName": "Ahmet"
}
```

---

### 🔒 **Protected Endpoints (JWT Token Gerekli)**

#### Kullanıcıları Listele
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/kullanicilar
```

#### Kullanıcı Detayı
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/kullanicilar/1
```

---

## 🔐 JWT Authentication

### Nasıl Çalışır?

1. **Register veya Login** yap → Token al
2. **Token'ı her istekte gönder:** `Authorization: Bearer <token>`
3. Token **24 saat** geçerli
4. **Refresh Token** ile yenile (30 gün geçerli)

### Örnek Kullanım
```bash
# 1. Login yap
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ahmet@test.com","password":"Pass123!"}' \
  | jq -r '.token')

# 2. Token ile kullanıcıları listele
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/kullanicilar
```

---

## 📖 Swagger UI Kullanımı

1. Tarayıcıda aç: http://localhost:8080/swagger-ui/index.html
2. **POST /api/auth/login** ile giriş yap
3. Token'ı kopyala
4. Sağ üstteki **🔓 Authorize** butonuna tıkla
5. Token'ı yapıştır (Bearer yazmadan)
6. Artık tüm endpoint'leri test edebilirsin!

---

## 🛠️ Teknolojiler

- Java 21
- Spring Boot 4.0.3
- Spring Security + JWT
- PostgreSQL 16
- Docker
- Swagger/OpenAPI

---

## 📂 Proje Yapısı

```
src/main/java/com/example/demo/
├── config/          → SecurityConfig, JwtProperties
├── controller/      → AuthController, KullaniciController  
├── dto/             → Request/Response DTO'lar
├── entity/          → Kullanici, RefreshToken
├── repository/      → JPA Repository'ler
├── security/        → JwtUtil, JwtAuthenticationFilter
└── service/         → Business logic
```

---

## 🔧 Yapılandırma

### Database (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/envanter_db
spring.datasource.username=efsora_user
spring.datasource.password=efsora123
```

### JWT
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000          # 24 saat
jwt.refresh-expiration=2592000000 # 30 gün
```

---

## 🐛 Sık Karşılaşılan Hatalar

**401 Unauthorized:** Token geçersiz veya eksik  
**403 Forbidden:** Token olmadan korumalı endpoint'e erişim  
**400 Bad Request:** Eksik veya hatalı veri

---

## 📝 Not

Bu proje öğrenme amaçlıdır. Production'da:
- Şifreleri environment variable'da sakla
- HTTPS kullan
- Rate limiting ekle

---

**🎉 Başarıyla kuruldu!**

### 🟢 Sağlık Kontrolü
```http
GET http://localhost:8080/api/kullanicilar/health
```

### 📋 Tüm Kullanıcıları Listele
```http
GET http://localhost:8080/api/kullanicilar
```

### 👤 ID ile Kullanıcı Bul
```http
GET http://localhost:8080/api/kullanicilar/1
```

### 📧 Email ile Kullanıcı Bul
```http
GET http://localhost:8080/api/kullanicilar/email/ahmet@efsora.com
```

### 🏢 Departmana Göre Listele
```http
GET http://localhost:8080/api/kullanicilar/departman/IT
```

### ✅ Aktif Kullanıcıları Listele
```http
GET http://localhost:8080/api/kullanicilar/aktif
```

### ➕ Yeni Kullanıcı Oluştur
```http
POST http://localhost:8080/api/kullanicilar
Content-Type: application/json

{
  "ad": "Ahmet",
  "soyad": "Yılmaz",
  "email": "ahmet@efsora.com",
  "departman": "IT"
}
```

### ✏️ Kullanıcı Güncelle
```http
PUT http://localhost:8080/api/kullanicilar/1
Content-Type: application/json

{
  "ad": "Mehmet",
  "soyad": "Kaya",
  "email": "mehmet@efsora.com",
  "departman": "HR"
}
```

### 🗑️ Kullanıcıyı Sil (Soft Delete)
```http
DELETE http://localhost:8080/api/kullanicilar/1
```

### ❌ Kullanıcıyı Kalıcı Sil
```http
DELETE http://localhost:8080/api/kullanicilar/1/kalici
```

---

## 🛠️ Teknoloji Stack

| Teknoloji | Açıklama |
|-----------|----------|
| **Java 21** | LTS versiyonu |
| **Spring Boot 4.0.3** | Backend framework |
| **PostgreSQL 16** | Veritabanı |
| **Lombok** | Boilerplate kod azaltma (getter/setter) |
| **JPA/Hibernate** | ORM (Object-Relational Mapping) |
| **Maven** | Bağımlılık yönetimi |
| **Docker** | PostgreSQL container |

---

## 📂 Proje Yapısı

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── entity/Kullanici.java
│   │   │   ├── repository/KullaniciRepository.java
│   │   │   ├── service/KullaniciService.java
│   │   │   ├── controller/KullaniciController.java
│   │   │   ├── exception/GlobalExceptionHandler.java
│   │   │   └── EfsoraBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🔧 Yapılandırma

### PostgreSQL Bağlantı Bilgileri
- **Database**: `envanter_db`
- **Username**: `efsora_user`
- **Password**: `efsora123`
- **Port**: `5432`

### Spring Boot Ayarları
- **Port**: `8080`
- **JPA DDL**: `update` (Tablolar otomatik oluşturulur)
- **SQL Loglama**: Aktif

---

## 📝 Örnek Kullanım (cURL)

### Yeni kullanıcı oluştur
```bash
curl -X POST http://localhost:8080/api/kullanicilar \
-H "Content-Type: application/json" \
-d '{
  "ad": "Ayşe",
  "soyad": "Demir",
  "email": "ayse@efsora.com",
  "departman": "Finans"
}'
```

### Tüm kullanıcıları listele
```bash
curl http://localhost:8080/api/kullanicilar
```

---

## 🧪 Test

```bash
./mvnw test
```

---

## 🐛 Hata Yönetimi

API şu durumlarda hata döner:
- ❌ Email zaten kayıtlı
- ❌ Zorunlu alanlar boş
- ❌ Geçersiz email formatı
- ❌ Kullanıcı bulunamadı

**Örnek Hata Response:**
```json
{
  "timestamp": "2026-03-02T12:00:00",
  "status": 400,
  "message": "Bu email adresi zaten kayıtlı: ahmet@efsora.com"
}
```

---

## 📖 Öğrenme Notları

### 1. Entity Nedir?
Veritabanındaki `kullanicilar` tablosunun Java'daki karşılığıdır.

### 2. Repository Nedir?
SQL yazmadan veritabanı işlemleri yapmamızı sağlar (Spring Data JPA magic ✨)

### 3. Service Nedir?
İş mantığının olduğu katmandır. Örnek: "Email zaten var mı?" kontrolü.

### 4. Controller Nedir?
Frontend'in bağlandığı API endpoint'lerini oluşturur.

---

## 🎯 Sıradaki Adımlar

- [ ] Envanter modülü ekle (Bilgisayar, Monitor, Telefon)
- [ ] JWT Authentication ekle
- [ ] Pagination (Sayfalama) ekle
- [ ] Swagger/OpenAPI dokümantasyonu
- [ ] Unit testler yaz

---

## 📞 İletişim

**Proje**: EfsoraBackend  
**Version**: 1.0.0  
**Java**: 21 (LTS)  
**Spring Boot**: 4.0.3

---

**🎉 Başarılı! API çalışıyor ve hazır!**

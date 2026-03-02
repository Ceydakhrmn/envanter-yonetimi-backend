# 🏢 Kurumsal Envanter ve Kullanıcı Yönetim Sistemi

**Backend API** - Java 21 + Spring Boot 4.0.3 + PostgreSQL

---

## 📚 Katmanlı Mimari (Layered Architecture)

```
src/main/java/com/example/demo/
├── entity/              → Veritabanı tabloları (Kullanici)
├── repository/          → Veritabanı işlemleri (KullaniciRepository)
├── service/             → İş mantığı (KullaniciService)
├── controller/          → REST API endpoint'leri (KullaniciController)
└── exception/           → Hata yönetimi (GlobalExceptionHandler)
```

---

## 🚀 Hızlı Başlangıç

### 1️⃣ PostgreSQL'i Docker ile Başlat

```bash
docker-compose up -d
```

✅ Bu komut:
- PostgreSQL 16 container'ını ayağa kaldırır
- `envanter_db` veritabanını oluşturur
- Port 5432'de dinlemeye başlar

### 2️⃣ Spring Boot Uygulamasını Çalıştır

```bash
./mvnw spring-boot:run
```

✅ Uygulama `http://localhost:8080` adresinde çalışacak

---

## 🔌 API Endpoint'leri

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

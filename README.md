# Efsora Labs Backend

Efsora Labs Backend, Spring Boot tabanli bir REST API uygulamasidir.
Kullanici yonetimi, JWT tabanli kimlik dogrulama ve refresh token akisi sunar.

## Ozellikler

- JWT Access Token + Refresh Token akisi
- Register / Login / Refresh / Logout endpoint'leri
- Kullanici CRUD endpoint'leri
- PostgreSQL veri tabani
- Swagger/OpenAPI dokumantasyonu
- Docker Compose ile tek komutta calisma

## Teknoloji Yigini

- Java 21
- Spring Boot 4.0.3
- Spring Security
- Spring Data JPA
- PostgreSQL 16
- Docker / Docker Compose

## Proje Ne Ise Yarar?

Bu servis, Efsora Labs icin backend tarafinda:

- kullanici kayit ve giris islemlerini,
- token tabanli oturum yonetimini,
- kullanici verilerinin guvenli sekilde saklanmasini

saglar.

## Kurulum (Docker)

1. Proje dizinine gelin:

```bash
cd backend
```

2. Ortam degiskenlerini ayarlayin (ornek):

```bash
export POSTGRES_DB=envanter_db
export POSTGRES_USER=efsora_user
export POSTGRES_PASSWORD=change_me
export JWT_SECRET=change_me_to_a_long_random_secret
```

3. Servisleri baslatin:

```bash
docker compose up --build -d
```

4. Durumu kontrol edin:

```bash
docker compose ps
```

5. Loglari izleyin:

```bash
docker compose logs -f
```

## API Endpoints

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/test`

### Kullanici

- `GET /api/kullanicilar`
- `GET /api/kullanicilar/{id}`
- `POST /api/kullanicilar`
- `PUT /api/kullanicilar/{id}`
- `DELETE /api/kullanicilar/{id}`
- `DELETE /api/kullanicilar/{id}/permanent`
- `GET /api/kullanicilar/active`
- `GET /api/kullanicilar/email/{email}`
- `GET /api/kullanicilar/departman/{departman}`
- `GET /api/kullanicilar/health`

## Ornek Login (curl)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.yilmaz@efsora.com",
    "password": "password123"
  }'
```

Basarili response icinde `token` ve `refreshToken` alanlari doner.

## JWT Sistemi Nasil Calisiyor?

1. Kullanici `login` veya `register` olur.
2. Sistem bir `access token` ve bir `refresh token` uretir.
3. Access token korumali endpoint'lerde `Authorization: Bearer <token>` ile gonderilir.
4. Access token suresi dolarsa `POST /api/auth/refresh` ile yeni access token alinir.
5. `POST /api/auth/logout` refresh token'i gecersizlestirir.

## Swagger

- URL: `http://localhost:8080/swagger-ui/index.html`

## Guvenlik Notu

- `JWT_SECRET` ve `POSTGRES_PASSWORD` gibi gizli degerler kod icine yazilmamalidir.
- Bu degerler environment variable olarak verilmistir.

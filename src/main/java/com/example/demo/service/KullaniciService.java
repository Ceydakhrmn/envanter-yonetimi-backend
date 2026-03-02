package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kullanıcı Service - İş mantığı burada
 * Repository ile Controller arasındaki köprü
 */
@Service
@RequiredArgsConstructor  // Lombok: final field'lar için constructor oluşturur
@Slf4j  // Lombok: Logger otomatik oluşturur (log.info(...) kullanabilirsin)
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;

    /**
     * Yeni kullanıcı oluştur
     */
    @Transactional
    public Kullanici kullaniciOlustur(Kullanici kullanici) {
        log.info("Yeni kullanıcı oluşturuluyor: {}", kullanici.getEmail());
        
        // Email kontrolü
        if (kullaniciRepository.existsByEmail(kullanici.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kayıtlı: " + kullanici.getEmail());
        }
        
        return kullaniciRepository.save(kullanici);
    }

    /**
     * Tüm kullanıcıları listele
     */
    public List<Kullanici> tumKullanicilar() {
        log.info("Tüm kullanıcılar listeleniyor");
        return kullaniciRepository.findAll();
    }

    /**
     * ID ile kullanıcı bul
     */
    public Kullanici kullaniciBul(Long id) {
        log.info("Kullanıcı aranıyor: ID={}", id);
        return kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: ID=" + id));
    }

    /**
     * Email ile kullanıcı bul
     */
    public Kullanici emailIleKullaniciBul(String email) {
        log.info("Kullanıcı aranıyor: Email={}", email);
        return kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: Email=" + email));
    }

    /**
     * Departmana göre kullanıcıları listele
     */
    public List<Kullanici> departmanaGoreListele(String departman) {
        log.info("Departmana göre listeleniyor: {}", departman);
        return kullaniciRepository.findByDepartman(departman);
    }

    /**
     * Kullanıcı güncelle
     */
    @Transactional
    public Kullanici kullaniciGuncelle(Long id, Kullanici yeniKullanici) {
        log.info("Kullanıcı güncelleniyor: ID={}", id);
        
        Kullanici mevcutKullanici = kullaniciBul(id);
        
        mevcutKullanici.setAd(yeniKullanici.getAd());
        mevcutKullanici.setSoyad(yeniKullanici.getSoyad());
        mevcutKullanici.setDepartman(yeniKullanici.getDepartman());
        
        // Email değişiyorsa, yeni email kontrolü
        if (!mevcutKullanici.getEmail().equals(yeniKullanici.getEmail())) {
            if (kullaniciRepository.existsByEmail(yeniKullanici.getEmail())) {
                throw new RuntimeException("Bu email adresi zaten kullanılıyor: " + yeniKullanici.getEmail());
            }
            mevcutKullanici.setEmail(yeniKullanici.getEmail());
        }
        
        return kullaniciRepository.save(mevcutKullanici);
    }

    /**
     * Kullanıcıyı sil (soft delete - aktif=false)
     */
    @Transactional
    public void kullaniciSil(Long id) {
        log.info("Kullanıcı siliniyor (deaktif): ID={}", id);
        Kullanici kullanici = kullaniciBul(id);
        kullanici.setAktif(false);
        kullaniciRepository.save(kullanici);
    }

    /**
     * Kullanıcıyı kalıcı sil (hard delete - veritabanından tamamen sil)
     */
    @Transactional
    public void kullaniciKaliciSil(Long id) {
        log.warn("Kullanıcı kalıcı olarak siliniyor: ID={}", id);
        kullaniciRepository.deleteById(id);
    }

    /**
     * Aktif kullanıcıları listele
     */
    public List<Kullanici> aktifKullanicilar() {
        log.info("Aktif kullanıcılar listeleniyor");
        return kullaniciRepository.findByAktifTrue();
    }
}

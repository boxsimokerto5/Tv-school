# Informasi Signature Key (Keystore) Permanen

Keystore ini telah dibuat dengan masa berlaku **100 tahun (36.500 hari)** hingga tahun **2126**, sehingga tanda tangan aplikasi (signature key) ini **permanen dan tidak akan pernah berubah**.

## Detail Keystore

- **Nama File Keystore**: `my-upload-key.jks`
- **Backup Base64**: `my-upload-key.jks.base64`
- **Key Alias**: `upload`
- **Keystore Password**: `geccko2026tvsekolah`
- **Key Password**: `geccko2026tvsekolah`
- **Algorithm**: RSA 2048-bit (SHA384withRSA)
- **Masa Berlaku**: 36.500 hari (~100 tahun, aktif hingga 2 Oktober 2126)
- **Distinguished Name (DName)**: `CN=GecckoCreator, OU=TV Sekolah, O=Geccko, L=Jakarta, ST=DKI Jakarta, C=ID`

### Fingerprint Sertifikat
- **SHA-1**: `47:1A:17:8B:7B:D2:C7:2B:46:47:05:93:10:14:83:E1:F6:3A:76:60`
- **SHA-256**: `6F:C5:6A:B0:20:21:04:20:FA:D4:2B:00:B1:C4:2A:C0:F5:AD:C3:8F:F6:10:47:5E:78:B3:D6:AF:4F:C4:55:FD`

---

## Integrasi GitHub Actions

Workflow GitHub Actions sudah dibuat di `.github/workflows/build.yml`.
Workflow akan otomatis:
1. Membuild **Release APK** (`app-release.apk`) yang sudah ditandatangani.
2. Membuild **Release AAB** (`app-release.aab`) untuk diunggah ke Google Play Console.
3. Membuild **Debug APK** untuk pengujian langsung.
4. Mengunggah semua artifact tersebut ke tab **Actions > Artifacts** di GitHub.

### Opsi GitHub Secrets (Opsional tapi Direkomendasikan)
Jika Anda ingin menyembunyikan keystore dari commit publik GitHub, Anda dapat menambahkan Secrets di repository GitHub Anda (**Settings > Secrets and variables > Actions > New repository secret**):

| Secret Name | Value |
|---|---|
| `KEYSTORE_BASE64` | Isi dari file `my-upload-key.jks.base64` |
| `STORE_PASSWORD` | `geccko2026tvsekolah` |
| `KEY_PASSWORD` | `geccko2026tvsekolah` |

*Catatan: Jika Secrets di atas tidak diisi, workflow secara otomatis menggunakan file `my-upload-key.jks` dan password default yang telah dikonfigurasi.*

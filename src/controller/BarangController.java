package controller;

import akun.Sesi;
import dao.BarangDAO;
import dao.KategoriDAO;
import model.Barang;
import model.Kategori;
import tampilan.FormBarang;
import tampilan.FormLogin;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * CONTROLLER: pengatur alur Form Barang (Praktikum 11).
 *
 * Alur setiap tindakan:
 * View (klik) -> Controller -> periksa hak akses -> Model (Barang.baru) -> DAO -> database
 *             <- Controller memilih pesan dan meminta View memperbarui tampilan.
 *
 * Controller tidak menyusun komponen Swing dan tidak berisi teks SQL.
 */
public final class BarangController {

    private final FormBarang view;
    private final Sesi sesi;
    private final BarangDAO barangDao;
    private final KategoriDAO kategoriDao;

    // ---------- Keadaan layar ----------
    private final List<Barang> daftarBarang = new ArrayList<Barang>();
    private final List<Kategori> daftarKategori = new ArrayList<Kategori>();
    private boolean sibuk = false;
    /** null = mode data baru; berisi kode = mode mengubah barang tersebut. */
    private String kodeTerpilih = null;
    private String kataKunci = "";

    /** Pekerjaan database yang mengubah data dan mengembalikan jumlah baris. */
    private interface Perubahan {
        int jalankan() throws SQLException;
    }

    /** Hasil pembacaan kategori dan barang dari database. */
    private static class DataTampilan {
        private List<Kategori> kategori;
        private List<Barang> barang;
    }

    public BarangController(FormBarang view, Sesi sesi, BarangDAO barangDao,
            KategoriDAO kategoriDao) {
        this.view = Objects.requireNonNull(view);
        this.sesi = Objects.requireNonNull(sesi, "Form barang hanya dapat dibuka setelah login.");
        this.barangDao = Objects.requireNonNull(barangDao);
        this.kategoriDao = Objects.requireNonNull(kategoriDao);

        view.tampilkanPengguna(
                "Masuk sebagai: " + sesi.getNama() + " (" + sesi.getUsername()
                + ") | Peran: " + sesi.getPeran().toUpperCase(),
                sesi.isAdmin()
                ? "Hak akses: menambah, mengubah, menghapus, mencari, dan melihat data."
                : "Hak akses: mencari dan melihat data (mode baca).",
                sesi.isAdmin());

        // Menghubungkan setiap event View dengan method Controller.
        view.onTambah(this::tambah);
        view.onUbah(this::ubah);
        view.onHapus(this::hapus);
        view.onBersihkan(this::bersihkan);
        view.onMuatUlang(() -> muatData(""));
        view.onKeluar(this::keluar);
        view.onCari(this::cari);
        view.onTampilkanSemua(this::tampilkanSemua);
        view.onPilihBaris(this::pilihBaris);
        view.onTutupJendela(this::tutupJendela);
        aturTampilan();
    }

    /** Merakit MVC setelah login berhasil: View + DAO + Controller, lalu menampilkan form. */
    public static void buka(Sesi sesi) {
        FormBarang view = new FormBarang();
        BarangController controller =
                new BarangController(view, sesi, new BarangDAO(), new KategoriDAO());
        view.setVisible(true);
        controller.muatData("");
    }

    // =====================================================================
    // KEADAAN TAMPILAN
    // =====================================================================

    private void aturSibuk(boolean nilai) {
        sibuk = nilai;
        aturTampilan();
    }

    /** Controller memutuskan; View hanya menjalankan. */
    private void aturTampilan() {
        view.aturKomponen(!sibuk, sesi.isAdmin(), !daftarKategori.isEmpty(), kodeTerpilih);
    }

    // =====================================================================
    // EVENT DARI VIEW
    // =====================================================================

    private void pilihBaris() {
        int baris = view.getBarisTerpilih();
        if (baris < 0 || baris >= daftarBarang.size()) {
            return;
        }
        Barang b = daftarBarang.get(baris);
        kodeTerpilih = b.getKode();
        view.isiForm(b);
        view.setStatus("Barang " + b.getKode() + " dipilih.");
        aturTampilan();
    }

    private void bersihkan() {
        kodeTerpilih = null;
        view.kosongkanForm();
        aturTampilan();
        view.fokusKeKode();
    }

    private void cari() {
        if (sibuk) {
            return;
        }
        String teks = view.getKataKunci().trim();
        if (teks.length() > 50) {
            view.tampilkanInfo("Kata kunci maksimal 50 karakter.");
            return;
        }
        kataKunci = teks;
        bersihkan();
        muatData("");
    }

    private void tampilkanSemua() {
        view.setKataKunci("");
        cari();
    }

    private void keluar() {
        if (sibuk) {
            view.tampilkanInfo("Tunggu sampai proses database selesai.");
            return;
        }
        view.tutup();
        new FormLogin().setVisible(true);
    }

    private void tutupJendela() {
        if (sibuk) {
            view.tampilkanInfo("Tunggu sampai proses database selesai.");
        } else {
            view.tutup();
        }
    }

    // =====================================================================
    // CREATE, UPDATE, DELETE (hak akses diperiksa di sini)
    // =====================================================================

    private void tambah() {
        if (!sesi.isAdmin()) {
            tolakAkses("menambah");
            return;
        }
        if (sibuk) {
            return;
        }
        if (kodeTerpilih != null) {
            view.tampilkanInfo("Anda sedang memilih barang " + kodeTerpilih
                    + ".\nKlik Bersihkan Form terlebih dahulu untuk menambah barang baru.");
            return;
        }
        final Barang b = bacaForm(view.getKodeInput());
        if (b == null) {
            return;
        }
        jalankanPerubahan("Menyimpan barang baru...",
                "Barang " + b.getKode() + " berhasil ditambahkan.",
                () -> barangDao.tambah(b));
    }

    private void ubah() {
        if (!sesi.isAdmin()) {
            tolakAkses("mengubah");
            return;
        }
        if (sibuk) {
            return;
        }
        if (kodeTerpilih == null) {
            view.tampilkanInfo("Klik dahulu barang yang akan diubah pada tabel.");
            return;
        }
        final Barang b = bacaForm(kodeTerpilih);
        if (b == null) {
            return;
        }
        jalankanPerubahan("Menyimpan perubahan...",
                "Perubahan barang " + b.getKode() + " berhasil disimpan.",
                () -> barangDao.ubah(b));
    }

    private void hapus() {
        if (!sesi.isAdmin()) {
            tolakAkses("menghapus");
            return;
        }
        if (sibuk) {
            return;
        }
        if (kodeTerpilih == null) {
            view.tampilkanInfo("Klik dahulu barang yang akan dihapus pada tabel.");
            return;
        }
        final String kode = kodeTerpilih;
        boolean yakin = view.konfirmasi("Konfirmasi Hapus",
                "Hapus barang " + kode + " - " + view.getNamaInput().trim() + "?\n"
                + "Data yang sudah dihapus tidak dapat dikembalikan.");
        if (!yakin) {
            view.setStatus("Penghapusan dibatalkan.");
            return;
        }
        jalankanPerubahan("Menghapus barang...",
                "Barang " + kode + " berhasil dihapus.",
                () -> barangDao.hapus(kode));
    }

    private void tolakAkses(String tindakan) {
        view.setStatus("Ditolak: peran " + sesi.getPeran() + " tidak boleh "
                + tindakan + " barang.");
        view.tampilkanPeringatan("Hak Akses",
                "Akses ditolak. Hanya admin yang boleh " + tindakan + " barang.");
    }

    /**
     * Mengubah isian View menjadi objek Barang.
     * Controller hanya mengubah teks jumlah menjadi angka;
     * aturan data diperiksa oleh Model melalui Barang.baru(...).
     * Mengembalikan null jika isian salah (pesan sudah ditampilkan).
     */
    private Barang bacaForm(String kode) {
        int jumlah;
        try {
            jumlah = Integer.parseInt(view.getJumlahInput().trim());
        } catch (NumberFormatException e) {
            view.tampilkanInfo(
                    "Jumlah harus bilangan bulat 0 sampai 2147483647, tanpa titik atau koma.");
            return null;
        }
        try {
            return Barang.baru(kode, view.getNamaInput(), view.getKategoriTerpilih(),
                    jumlah, view.getKeteranganInput());
        } catch (IllegalArgumentException e) {
            view.tampilkanInfo(e.getMessage());
            return null;
        }
    }

    /** Menjalankan tambah/ubah/hapus di latar belakang, lalu memuat ulang daftar. */
    private void jalankanPerubahan(final String pesanProses, final String pesanBerhasil,
            final Perubahan perubahan) {
        aturSibuk(true);
        view.setStatus(pesanProses);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws SQLException {
                return perubahan.jalankan();
            }

            @Override
            protected void done() {
                boolean muatUlang = false;
                String pesanMuat = "";
                try {
                    int baris = get();
                    if (baris == 1) {
                        muatUlang = true;
                        pesanMuat = pesanBerhasil + " ";
                        view.tampilkanInfo(pesanBerhasil);
                    } else if (baris == 0) {
                        muatUlang = true;
                        pesanMuat = "Data tidak ditemukan. ";
                        view.tampilkanPeringatan("Data Berubah",
                                "Barang tidak ditemukan di database.\n"
                                + "Kemungkinan sudah dihapus oleh pengguna lain.\n"
                                + "Daftar akan dimuat ulang.");
                    } else {
                        view.setStatus("Jumlah baris terdampak tidak wajar: " + baris);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    view.setStatus("Proses terhenti. Periksa data sebelum mengulang.");
                } catch (ExecutionException e) {
                    view.setStatus("Proses gagal. Data tidak berubah.");
                    view.tampilkanError("Informasi Database",
                            jelaskanError("Perubahan data tidak berhasil.", e.getCause()));
                } finally {
                    aturSibuk(false);
                }
                if (muatUlang) {
                    bersihkan();
                    muatData(pesanMuat);
                }
            }
        }.execute();
    }

    // =====================================================================
    // READ: memuat dan mencari data
    // =====================================================================

    private void muatData(final String pesanAwal) {
        if (sibuk) {
            return;
        }
        final String kunci = kataKunci;
        Kategori pilihanLama = view.getKategoriTerpilih();
        final Integer idTerpilih = pilihanLama == null ? null : pilihanLama.getId();
        aturSibuk(true);
        view.setStatus(pesanAwal + "Memuat data dari database...");
        new SwingWorker<DataTampilan, Void>() {
            @Override
            protected DataTampilan doInBackground() throws SQLException {
                DataTampilan data = new DataTampilan();
                data.kategori = kategoriDao.semua();
                data.barang = barangDao.cari(kunci);
                return data;
            }

            @Override
            protected void done() {
                try {
                    DataTampilan data = get();
                    daftarKategori.clear();
                    daftarKategori.addAll(data.kategori);
                    view.isiKategori(daftarKategori, idTerpilih);
                    daftarBarang.clear();
                    daftarBarang.addAll(data.barang);
                    view.isiTabel(daftarBarang);

                    String status;
                    if (kunci.isEmpty()) {
                        status = pesanAwal + "Data terbaru: " + daftarBarang.size() + " barang.";
                    } else {
                        status = pesanAwal + "Hasil pencarian \"" + kunci + "\": "
                                + daftarBarang.size() + " barang.";
                    }
                    if (daftarKategori.isEmpty()) {
                        status += " Kategori kosong: laporkan kepada dosen.";
                    }
                    view.setStatus(status);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    view.setStatus("Pemuatan terhenti. Klik Muat Ulang Data.");
                } catch (ExecutionException e) {
                    view.setStatus(pesanAwal
                            + "Gagal memuat. Tabel belum diperbarui; klik Muat Ulang Data.");
                    view.tampilkanError("Informasi Database",
                            jelaskanError(pesanAwal + "Pembacaan data gagal.", e.getCause()));
                } finally {
                    aturSibuk(false);
                }
            }
        }.execute();
    }

    // =====================================================================
    // MENERJEMAHKAN KODE ERROR DATABASE MENJADI PESAN
    // =====================================================================

    private String jelaskanError(String konteks, Throwable sebab) {
        String pesan = konteks;
        if (sebab instanceof SQLException) {
            SQLException e = (SQLException) sebab;
            int kode = e.getErrorCode();
            if (kode == 1062) {
                pesan += "\nKode barang sudah digunakan. Gunakan kode lain.";
            } else if (kode == 1451) {
                pesan += "\nBarang ini sudah tercatat pada data peminjaman,"
                        + "\nsehingga tidak boleh dihapus agar riwayat tetap utuh.";
            } else if (kode == 1142) {
                pesan += "\nAkun database belum memiliki hak yang diperlukan."
                        + "\nPeriksa GRANT pada Praktikum 10 Langkah 4.";
            } else if (kode == 1452) {
                pesan += "\nKategori tidak ditemukan. Klik Muat Ulang Data, lalu pilih kategori lagi.";
            } else if (kode == 1406) {
                pesan += "\nIsian terlalu panjang untuk kolom database.";
            } else if (kode == 4025 || kode == 3819) {
                pesan += "\nNilai melanggar aturan database (CHECK).";
            } else {
                pesan += "\nPeriksa MySQL, konfigurasi koneksi, driver, dan struktur tabel.";
            }
            pesan += "\nSQLState: " + e.getSQLState() + "; kode: " + kode;
        } else if (sebab != null) {
            pesan += "\nJenis gangguan: " + sebab.getClass().getSimpleName();
        }
        return pesan;
    }
}
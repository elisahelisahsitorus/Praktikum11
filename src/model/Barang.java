package model;

import java.io.Serializable;

/**
 * MODEL: satu baris tabel barang beserta nama kategorinya.
 *
 * Mulai Praktikum 11, aturan data barang ditulis DI SINI, bukan di form.
 * - Data dari isian pengguna WAJIB dibuat lewat Barang.baru(...), yang memeriksa aturan.
 * - Constructor dipakai DAO untuk data yang sudah tersimpan di database.
 * Class ini tidak mengenal Swing (javax.swing) dan tidak mengenal SQL (java.sql).
 */
public final class Barang implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Batas panjang mengikuti kolom tabel barang (Praktikum 5). */
    public static final int MAKS_KODE = 20;
    public static final int MAKS_NAMA = 100;
    public static final int MAKS_KETERANGAN = 255;

    private final String kode;
    private final String nama;
    private final int idKategori;
    private final String namaKategori;
    private final int jumlahTersedia;
    private final String keterangan;

    public Barang(String kode, String nama, int idKategori, String namaKategori,
            int jumlahTersedia, String keterangan) {
        this.kode = kode;
        this.nama = nama;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.jumlahTersedia = jumlahTersedia;
        this.keterangan = keterangan == null ? "" : keterangan;
    }

    /**
     * Membuat Barang dari isian pengguna sambil memeriksa aturan data.
     * Jika ada aturan yang dilanggar, method ini melempar IllegalArgumentException
     * berisi pesan yang siap ditampilkan kepada pengguna.
     */
    public static Barang baru(String kode, String nama, Kategori kategori,
            int jumlahTersedia, String keterangan) {
        String k = kode == null ? "" : kode.trim();
        String n = nama == null ? "" : nama.trim();
        String ket = keterangan == null ? "" : keterangan.trim();

        if (k.isEmpty() || n.isEmpty()) {
            throw new IllegalArgumentException("Kode dan nama barang wajib diisi.");
        }
        if (k.length() > MAKS_KODE || n.length() > MAKS_NAMA || ket.length() > MAKS_KETERANGAN) {
            throw new IllegalArgumentException(
                    "Batas karakter: kode 20, nama 100, keterangan 255.");
        }
        if (kategori == null) {
            throw new IllegalArgumentException(
                    "Pilih kategori. Jika kosong, klik Muat Ulang Data.");
        }
        if (jumlahTersedia < 0) {
            throw new IllegalArgumentException("Jumlah tersedia tidak boleh negatif.");
        }
        return new Barang(k, n, kategori.getId(), kategori.getNama(), jumlahTersedia, ket);
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    public int getIdKategori() {
        return idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public int getJumlahTersedia() {
        return jumlahTersedia;
    }

    public String getKeterangan() {
        return keterangan;
    }
}
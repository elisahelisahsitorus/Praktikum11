package uji;

import model.Barang;
import model.Kategori;

/**
 * Menguji aturan Model Barang TANPA form dan TANPA database.
 * Jalankan: klik kanan file ini > Run File (Shift+F6).
 */
public final class UjiModelBarang {

    private static int lulus = 0;
    private static int gagal = 0;

    /** Pekerjaan yang membuat objek Barang. */
    private interface Pembuat {
        Barang buat();
    }

    private UjiModelBarang() {
    }

    public static void main(String[] args) {
        Kategori input = new Kategori(1, "Perangkat Input");
        String nama100 = ulang("N", 100);
        String nama101 = ulang("N", 101);

        System.out.println("=== UJI MODEL BARANG - PRAKTIKUM 11 ===");
        System.out.println();

        harusDiterima("M01 Data benar", () -> Barang.baru("P11-001", "Mouse Uji", input, 5, "Rak A"));
        harusDitolak("M02 Kode kosong", () -> Barang.baru("", "Mouse Uji", input, 5, ""));
        harusDitolak("M03 Nama hanya spasi", () -> Barang.baru("P11-002", "   ", input, 5, ""));
        harusDitolak("M04 Kode 21 karakter",
                () -> Barang.baru("123456789012345678901", "Mouse Uji", input, 5, ""));
        harusDiterima("M05 Nama tepat 100 karakter", () -> Barang.baru("P11-003", nama100, input, 1, ""));
        harusDitolak("M06 Nama 101 karakter", () -> Barang.baru("P11-004", nama101, input, 1, ""));
        harusDitolak("M07 Kategori belum dipilih", () -> Barang.baru("P11-005", "Mouse Uji", null, 1, ""));
        harusDitolak("M08 Jumlah -1", () -> Barang.baru("P11-006", "Mouse Uji", input, -1, ""));
        harusDiterima("M09 Jumlah 0", () -> Barang.baru("P11-007", "Mouse Uji", input, 0, ""));
        harusDiterima("M10 Keterangan null menjadi teks kosong",
                () -> Barang.baru("P11-008", "Mouse Uji", input, 1, null));

        Barang rapi = Barang.baru("  P11-009  ", "  Mouse Uji  ", input, 2, "  Rak B  ");
        periksa("M11 Spasi di tepi dipangkas",
                "P11-009".equals(rapi.getKode()) && "Mouse Uji".equals(rapi.getNama())
                && "Rak B".equals(rapi.getKeterangan()));

        // Tugas individu (Bagian G): tambahkan uji M12 di sini.

        System.out.println();
        System.out.println("HASIL: " + lulus + " lulus, " + gagal + " gagal");
        System.out.println(gagal == 0 ? "=== SEMUA UJI MODEL LULUS ===" : "=== ADA UJI YANG GAGAL ===");
    }

    private static void harusDiterima(String nama, Pembuat pembuat) {
        try {
            pembuat.buat();
            periksa(nama + " -> diterima", true);
        } catch (IllegalArgumentException e) {
            periksa(nama + " -> seharusnya diterima, tetapi ditolak: " + e.getMessage(), false);
        }
    }

    private static void harusDitolak(String nama, Pembuat pembuat) {
        try {
            pembuat.buat();
            periksa(nama + " -> seharusnya ditolak, tetapi diterima", false);
        } catch (IllegalArgumentException e) {
            periksa(nama + " -> ditolak: " + e.getMessage(), true);
        }
    }

    private static void periksa(String keterangan, boolean benar) {
        if (benar) {
            lulus++;
            System.out.println("[LULUS] " + keterangan);
        } else {
            gagal++;
            System.out.println("[GAGAL] " + keterangan);
        }
    }

    private static String ulang(String huruf, int kali) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kali; i++) {
            sb.append(huruf);
        }
        return sb.toString();
    }
}
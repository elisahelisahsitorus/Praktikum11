package tampilan;

import model.Barang;
import model.Kategori;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * VIEW: jendela data barang (Praktikum 11).
 *
 * Tugas View hanya tiga:
 * 1. menyusun komponen,
 * 2. memberikan isian pengguna kepada Controller (method get...),
 * 3. menampilkan data dan pesan yang diminta Controller.
 *
 * View TIDAK berisi SQL, TIDAK memanggil DAO, dan TIDAK memutuskan hak akses.
 * Ketika tombol diklik, View hanya menjalankan aksi yang dipasang Controller (method on...).
 */
public class FormBarang extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Runnable TANPA_AKSI = () -> { };

    // ---------- Komponen identitas pengguna ----------
    private final JLabel lblPengguna = new JLabel(" ");
    private final JLabel lblHak = new JLabel(" ");

    // ---------- Komponen pencarian ----------
    private final JTextField txtCari = new JTextField(22);
    private final JButton btnCari = new JButton("Cari");
    private final JButton btnSemua = new JButton("Tampilkan Semua");

    // ---------- Komponen form ----------
    private final JPanel pnlForm = new JPanel(new GridBagLayout());
    private final JTextField txtKode = new JTextField(20);
    private final JTextField txtNama = new JTextField(20);
    private final JComboBox<Kategori> cmbKategori = new JComboBox<Kategori>();
    private final JTextField txtJumlah = new JTextField("0", 20);
    private final JTextField txtKeterangan = new JTextField(20);
    private final JLabel lblMode = new JLabel(" ");
    private final JButton btnTambah = new JButton("Tambah");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBersihkan = new JButton("Bersihkan Form");
    private final JButton btnMuat = new JButton("Muat Ulang Data");
    private final JButton btnKeluar = new JButton("Keluar Akun");
    private final JLabel lblStatus = new JLabel("Menyiapkan aplikasi...");

    // ---------- Tabel ----------
    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new Object[]{"Kode", "Nama Barang", "Kategori", "Jumlah", "Keterangan"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? Integer.class : String.class;
        }
    };
    private final JTable tabel = new JTable(modelTabel);

    // ---------- Aksi yang dipasang oleh Controller ----------
    // (transient: aksi tidak ikut disimpan jika objek jendela diserialisasi)
    private transient Runnable aksiTambah = TANPA_AKSI;
    private transient Runnable aksiUbah = TANPA_AKSI;
    private transient Runnable aksiHapus = TANPA_AKSI;
    private transient Runnable aksiBersihkan = TANPA_AKSI;
    private transient Runnable aksiMuatUlang = TANPA_AKSI;
    private transient Runnable aksiKeluar = TANPA_AKSI;
    private transient Runnable aksiCari = TANPA_AKSI;
    private transient Runnable aksiTampilkanSemua = TANPA_AKSI;
    private transient Runnable aksiPilihBaris = TANPA_AKSI;
    private transient Runnable aksiTutup = TANPA_AKSI;

    public FormBarang() {
        super("Inventori Laboratorium | P11 - MVC");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                aksiTutup.run();
            }
        });
        susunTampilan();
        hubungkanKomponen();
        pack();
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
    }

    // =====================================================================
    // 1. MENYUSUN KOMPONEN
    // =====================================================================

    private void susunTampilan() {
        JPanel utama = new JPanel(new BorderLayout(12, 12));
        utama.setBorder(new EmptyBorder(18, 18, 18, 18));
        utama.setBackground(new Color(238, 243, 249));
        setContentPane(utama);

        JLabel judul = new JLabel("INVENTORI LABORATORIUM");
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));
        judul.setForeground(new Color(24, 49, 83));
        lblPengguna.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel infoPengguna = new JPanel(new BorderLayout(0, 2));
        infoPengguna.setOpaque(false);
        infoPengguna.add(lblPengguna, BorderLayout.NORTH);
        infoPengguna.add(lblHak, BorderLayout.SOUTH);
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.add(judul, BorderLayout.NORTH);
        header.add(infoPengguna, BorderLayout.SOUTH);
        utama.add(header, BorderLayout.NORTH);

        pnlForm.setBorder(BorderFactory.createTitledBorder("Data Barang"));
        tambahBaris(pnlForm, 0, "Kode barang", txtKode);
        tambahBaris(pnlForm, 1, "Nama barang", txtNama);
        tambahBaris(pnlForm, 2, "Kategori", cmbKategori);
        tambahBaris(pnlForm, 3, "Jumlah tersedia", txtJumlah);
        tambahBaris(pnlForm, 4, "Keterangan (boleh kosong)", txtKeterangan);
        lblMode.setFont(new Font("SansSerif", Font.BOLD, 12));
        GridBagConstraints gm = new GridBagConstraints();
        gm.gridx = 0;
        gm.gridy = 5;
        gm.gridwidth = 2;
        gm.anchor = GridBagConstraints.WEST;
        gm.insets = new Insets(4, 8, 0, 8);
        pnlForm.add(lblMode, gm);

        btnTambah.setToolTipText("Simpan barang baru (Ctrl+S). Khusus admin.");
        btnUbah.setToolTipText("Simpan perubahan barang yang dipilih. Khusus admin.");
        btnHapus.setToolTipText("Hapus barang yang dipilih (tombol Delete pada tabel). Khusus admin.");
        btnBersihkan.setToolTipText("Kosongkan form dan kembali ke mode data baru");
        btnKeluar.setToolTipText("Tutup sesi ini dan kembali ke jendela login");
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        tombol.add(btnTambah);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnBersihkan);
        tombol.add(btnMuat);
        tombol.add(btnKeluar);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 6;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 6, 8, 6);
        pnlForm.add(tombol, g);

        JPanel pnlCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlCari.setOpaque(false);
        pnlCari.add(new JLabel("Cari (kode / nama / kategori):"));
        pnlCari.add(txtCari);
        pnlCari.add(btnCari);
        pnlCari.add(btnSemua);

        tabel.setRowHeight(26);
        tabel.setAutoCreateRowSorter(true);
        tabel.setFillsViewportHeight(true);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(860, 230));
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Daftar Barang - klik satu baris untuk memilih"));

        JPanel atas = new JPanel(new BorderLayout(0, 8));
        atas.setOpaque(false);
        atas.add(pnlForm, BorderLayout.NORTH);
        atas.add(pnlCari, BorderLayout.SOUTH);
        JPanel tengah = new JPanel(new BorderLayout(10, 10));
        tengah.setOpaque(false);
        tengah.add(atas, BorderLayout.NORTH);
        tengah.add(scroll, BorderLayout.CENTER);
        utama.add(tengah, BorderLayout.CENTER);
        utama.add(lblStatus, BorderLayout.SOUTH);
    }

    private void tambahBaris(JPanel panel, int baris, String teks, JComponent isi) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = baris;
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        panel.add(new JLabel(teks), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(isi, g);
    }

    /**
     * Setiap tombol hanya menjalankan aksi yang dipasang Controller.
     * View tidak tahu apa yang terjadi setelah tombol diklik.
     */
    private void hubungkanKomponen() {
        btnTambah.addActionListener(e -> aksiTambah.run());
        btnUbah.addActionListener(e -> aksiUbah.run());
        btnHapus.addActionListener(e -> aksiHapus.run());
        btnBersihkan.addActionListener(e -> aksiBersihkan.run());
        btnMuat.addActionListener(e -> aksiMuatUlang.run());
        btnKeluar.addActionListener(e -> aksiKeluar.run());
        btnCari.addActionListener(e -> aksiCari.run());
        txtCari.addActionListener(e -> aksiCari.run());
        btnSemua.addActionListener(e -> aksiTampilkanSemua.run());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                aksiPilihBaris.run();
            }
        });

        // Jalur keyboard: Ctrl+S = Tambah, tombol Delete pada tabel = Hapus.
        // Jalur ini tetap aktif walaupun tombolnya abu-abu, sehingga
        // pemeriksaan hak akses di Controller benar-benar diuji.
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "tambahBarang");
        root.getActionMap().put("tambahBarang", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                aksiTambah.run();
            }
        });
        tabel.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "hapusBarang");
        tabel.getActionMap().put("hapusBarang", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                aksiHapus.run();
            }
        });
    }

    // =====================================================================
    // 2. MEMASANG AKSI (dipanggil oleh Controller)
    // =====================================================================

    public void onTambah(Runnable aksi) {
        aksiTambah = Objects.requireNonNull(aksi);
    }

    public void onUbah(Runnable aksi) {
        aksiUbah = Objects.requireNonNull(aksi);
    }

    public void onHapus(Runnable aksi) {
        aksiHapus = Objects.requireNonNull(aksi);
    }

    public void onBersihkan(Runnable aksi) {
        aksiBersihkan = Objects.requireNonNull(aksi);
    }

    public void onMuatUlang(Runnable aksi) {
        aksiMuatUlang = Objects.requireNonNull(aksi);
    }

    public void onKeluar(Runnable aksi) {
        aksiKeluar = Objects.requireNonNull(aksi);
    }

    public void onCari(Runnable aksi) {
        aksiCari = Objects.requireNonNull(aksi);
    }

    public void onTampilkanSemua(Runnable aksi) {
        aksiTampilkanSemua = Objects.requireNonNull(aksi);
    }

    public void onPilihBaris(Runnable aksi) {
        aksiPilihBaris = Objects.requireNonNull(aksi);
    }

    public void onTutupJendela(Runnable aksi) {
        aksiTutup = Objects.requireNonNull(aksi);
    }

    // =====================================================================
    // 3. MEMBERIKAN ISIAN KEPADA CONTROLLER (teks apa adanya)
    // =====================================================================

    public String getKodeInput() {
        return txtKode.getText();
    }

    public String getNamaInput() {
        return txtNama.getText();
    }

    public Kategori getKategoriTerpilih() {
        return (Kategori) cmbKategori.getSelectedItem();
    }

    public String getJumlahInput() {
        return txtJumlah.getText();
    }

    public String getKeteranganInput() {
        return txtKeterangan.getText();
    }

    public String getKataKunci() {
        return txtCari.getText();
    }

    public void setKataKunci(String teks) {
        txtCari.setText(teks);
    }

    /**
     * Nomor baris DATA yang dipilih, atau -1 jika tidak ada.
     * Tabel dapat diurutkan, jadi nomor baris layar diubah dulu ke nomor baris data.
     */
    public int getBarisTerpilih() {
        int barisTampil = tabel.getSelectedRow();
        if (barisTampil < 0) {
            return -1;
        }
        return tabel.convertRowIndexToModel(barisTampil);
    }

    // =====================================================================
    // 4. MENAMPILKAN DATA DAN PESAN (dipanggil oleh Controller)
    // =====================================================================

    public void tampilkanPengguna(String teksPengguna, String teksHak, boolean bolehMengubah) {
        lblPengguna.setText(teksPengguna);
        lblHak.setText(teksHak);
        lblHak.setForeground(bolehMengubah ? new Color(20, 110, 50) : new Color(170, 80, 0));
        pnlForm.setBorder(BorderFactory.createTitledBorder(bolehMengubah
                ? "Data Barang" : "Data Barang (hanya dapat dilihat)"));
    }

    /** Mengaktifkan atau menonaktifkan komponen sesuai keputusan Controller. */
    public void aturKomponen(boolean siap, boolean bolehMengubah, boolean adaKategori,
            String kodeTerpilih) {
        boolean modeBaru = kodeTerpilih == null;

        txtKode.setEnabled(siap && bolehMengubah && modeBaru);
        txtNama.setEnabled(siap && bolehMengubah);
        cmbKategori.setEnabled(siap && bolehMengubah);
        txtJumlah.setEnabled(siap && bolehMengubah);
        txtKeterangan.setEnabled(siap && bolehMengubah);

        btnTambah.setEnabled(siap && bolehMengubah && modeBaru && adaKategori);
        btnUbah.setEnabled(siap && bolehMengubah && !modeBaru && adaKategori);
        btnHapus.setEnabled(siap && bolehMengubah && !modeBaru);
        btnBersihkan.setEnabled(siap);
        btnMuat.setEnabled(siap);
        btnKeluar.setEnabled(siap);
        txtCari.setEnabled(siap);
        btnCari.setEnabled(siap);
        btnSemua.setEnabled(siap);
        tabel.setEnabled(siap);

        lblMode.setText(modeBaru
                ? "Mode: data baru. Isi form lalu klik Tambah, atau klik baris tabel untuk memilih."
                : "Mode: barang " + kodeTerpilih + " dipilih. Kode tidak dapat diubah.");
        lblMode.setForeground(modeBaru ? new Color(60, 60, 60) : new Color(20, 70, 160));
    }

    public void isiForm(Barang b) {
        txtKode.setText(b.getKode());
        txtNama.setText(b.getNama());
        pilihKategori(b.getIdKategori());
        txtJumlah.setText(String.valueOf(b.getJumlahTersedia()));
        txtKeterangan.setText(b.getKeterangan());
    }

    public void kosongkanForm() {
        tabel.clearSelection();
        txtKode.setText("");
        txtNama.setText("");
        txtJumlah.setText("0");
        txtKeterangan.setText("");
        if (cmbKategori.getItemCount() > 0) {
            cmbKategori.setSelectedIndex(0);
        }
    }

    public void fokusKeKode() {
        txtKode.requestFocusInWindow();
    }

    /** Mengisi combo kategori; pilihan lama dipertahankan jika ID-nya masih ada. */
    public void isiKategori(List<Kategori> daftar, Integer idDipertahankan) {
        cmbKategori.removeAllItems();
        for (Kategori k : daftar) {
            cmbKategori.addItem(k);
        }
        if (idDipertahankan != null) {
            pilihKategori(idDipertahankan);
        }
        if (cmbKategori.getSelectedIndex() < 0 && cmbKategori.getItemCount() > 0) {
            cmbKategori.setSelectedIndex(0);
        }
    }

    private void pilihKategori(int idKategori) {
        cmbKategori.setSelectedIndex(-1);
        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
            if (cmbKategori.getItemAt(i).getId() == idKategori) {
                cmbKategori.setSelectedIndex(i);
                return;
            }
        }
    }

    public void isiTabel(List<Barang> daftar) {
        modelTabel.setRowCount(0);
        for (Barang b : daftar) {
            modelTabel.addRow(new Object[]{
                b.getKode(), b.getNama(), b.getNamaKategori(),
                b.getJumlahTersedia(), b.getKeterangan()
            });
        }
    }

    public void setStatus(String teks) {
        lblStatus.setText(teks);
    }

    public void tampilkanInfo(String pesan) {
        JOptionPane.showMessageDialog(this, pesan);
    }

    public void tampilkanPeringatan(String judul, String pesan) {
        JOptionPane.showMessageDialog(this, pesan, judul, JOptionPane.WARNING_MESSAGE);
    }

    public void tampilkanError(String judul, String pesan) {
        JOptionPane.showMessageDialog(this, pesan, judul, JOptionPane.ERROR_MESSAGE);
    }

    /** Mengembalikan true hanya jika pengguna memilih Yes. */
    public boolean konfirmasi(String judul, String pesan) {
        int jawab = JOptionPane.showConfirmDialog(this, pesan, judul,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return jawab == JOptionPane.YES_OPTION;
    }

    public void tutup() {
        dispose();
    }

    /** Menjalankan FormBarang langsung akan membuka login terlebih dahulu. */
    public static void main(String[] args) {
        FormLogin.main(args);
    }
}
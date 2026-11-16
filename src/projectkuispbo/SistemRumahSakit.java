package ProjectKuisPBO;

import java.io.*;
import java.util.*;

public class SistemRumahSakit implements Serializable {
    private static final long serialVersionUID = 1L;
    private String namaRS;
    private HashMap<String, Pasien> daftarPasien;
    private HashMap<String, LayananMedis> daftarLayanan;
    private ArrayList<Transaksi> daftarTransaksi;

    public SistemRumahSakit(String namaRS) {
        this.namaRS = namaRS;
        this.daftarPasien = new HashMap<>();
        this.daftarLayanan = new HashMap<>();
        this.daftarTransaksi = new ArrayList<>();
    }

    public void tambahPasien(String id, String nama) {
        daftarPasien.put(id, new Pasien(id, nama));
    }

    public void tambahLayananMedis(String id, String nama, int kuota) {
        daftarLayanan.put(id, new LayananMedis(id, nama, kuota));
    }

    // DIGIT 6: Parsing format ID>ACT-01.KD>OBJ-A.JML>3.|ID>ACT-02...
    public void jalankanTransaksiDariFile(String namaFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(namaFile));
        StringBuilder isiFile = new StringBuilder();
        String baris;
        while ((baris = br.readLine()) != null) {
            isiFile.append(baris);
        }
        br.close();

        // Pecah berdasarkan tanda pipe '|'
        String[] blokTransaksi = isiFile.toString().split("\\|");

        for (String blok : blokTransaksi) {
            blok = blok.trim();
            if (blok.isEmpty()) continue;

            try {
                // Pecah bagian dalam blok berdasarkan titik '.'
                String[] bagian = blok.split("\\.");
                if (bagian.length < 3) {
                    throw new TransaksiTidakValidException("Format blok tidak lengkap: " + blok);
                }

                // Ambil nilai setelah tanda '>'
                String idPasien = bagian[0].split(">")[1].trim();
                String idLayanan = bagian[1].split(">")[1].trim();
                int jumlah = Integer.parseInt(bagian[2].split(">")[1].trim());

                // Validasi keberadaan Pasien & Layanan
                Pasien pasien = daftarPasien.get(idPasien);
                if (pasien == null) {
                    throw new TransaksiTidakValidException("ID Pasien tidak ditemukan: " + idPasien);
                }

                LayananMedis layanan = daftarLayanan.get(idLayanan);
                if (layanan == null) {
                    throw new TransaksiTidakValidException("Kode Layanan tidak ditemukan: " + idLayanan);
                }

                if (jumlah <= 0 || layanan.getKuota() < jumlah) {
                    throw new TransaksiTidakValidException("Kuota tidak mencukupi untuk " + layanan.getNama() + " (Diminta: " + jumlah + ", Sisa: " + layanan.getKuota() + ")");
                }

                // Proses transaksi
                layanan.kurangiKuota(jumlah);
                daftarTransaksi.add(new Transaksi(pasien, layanan, jumlah));
                System.out.println("[BERHASIL] " + pasien.getNama() + " mengambil " + layanan.getNama() + " (" + jumlah + ")");

            } catch (TransaksiTidakValidException e) {
                System.out.println("[GAGAL VALIDASI] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[ERROR PARSING] Gagal memproses blok '" + blok + "': " + e.getMessage());
            }
        }
    }

    // DIGIT 7: Serialization ke sistem_state.ser & Laporan HashMap.values() ke laporan.txt
    public void simpanLaporanAkhir() {
        // 1. Object Serialization ke sistem_state.ser
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("sistem_state.ser"))) {
            oos.writeObject(this);
            System.out.println("[SERIALIZATION] Status sistem berhasil disimpan ke sistem_state.ser");
        } catch (IOException e) {
            System.out.println("[ERROR SERIALIZATION] " + e.getMessage());
        }

        // 2. Penulisan laporan.txt menggunakan values() pada HashMap Pasien
        try (PrintWriter writer = new PrintWriter(new FileWriter("laporan.txt"))) {
            writer.println("=== DAFTAR AKTOR AKTIF (" + namaRS + ") ===");
            for (Pasien p : daftarPasien.values()) {
                writer.println("ID: " + p.getId() + " | Nama: " + p.getNama());
            }
            System.out.println("[FILE I/O] Laporan berhasil ditulis ke laporan.txt");
        } catch (IOException e) {
            System.out.println("[ERROR FILE I/O] " + e.getMessage());
        }
    }
}
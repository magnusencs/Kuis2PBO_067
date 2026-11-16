package ProjectKuisPBO;

public class LayananMedis extends Entitas {
    private static final long serialVersionUID = 1L;
    private int kuota;

    public LayananMedis(String id, String nama, int kuota) {
        super(id, nama);
        this.kuota = kuota;
    }

    public int getKuota() {
        return kuota;
    }

    public void kurangiKuota(int jumlah) {
        this.kuota -= jumlah;
    }

    @Override
    public String getInfoRingkas() {
        return getId() + " - " + getNama() + " (Sisa Kuota: " + kuota + ")";
    }
}
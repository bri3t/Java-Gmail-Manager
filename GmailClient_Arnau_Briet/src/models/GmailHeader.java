
package models;

/**
 *
 * @author arnau
 */
public class GmailHeader {
    
    String fecha;
    String asunto;
    String from;
    int idMessage;

    public GmailHeader(String fecha, String asunto, String from, int idMessage) {
        this.fecha = fecha;
        this.asunto = asunto;
        this.from = from;
        this.idMessage = idMessage;
    }

    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }
 
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    
    
}

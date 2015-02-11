import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;

import java.awt.geom.Rectangle2D;

/**
 * Created by iliakan on 11.02.15.
 */
public class LinkInfo {

    protected PDNamedDestination destination;
    protected Rectangle2D.Float rectangle;
    protected String text;

    public LinkInfo(PDNamedDestination destination, Rectangle2D.Float rectangle, String text) {
        this.destination = destination;
        this.rectangle = rectangle;
        this.text = text;
    }

    @Override
    public String toString() {
        return "LinkInfo{" +
                "destination=" + destination.getNamedDestination() +
                ", rectangle=" + rectangle +
                ", text='" + text + '\'' +
                '}';
    }

    public PDNamedDestination getDestination() {
        return destination;
    }

    public void setDestination(PDNamedDestination destination) {
        this.destination = destination;
    }

    public Rectangle2D.Float getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle2D.Float rectangle) {
        this.rectangle = rectangle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkInfo linkInfo = (LinkInfo) o;

        if (!destination.equals(linkInfo.destination)) return false;
        if (!rectangle.equals(linkInfo.rectangle)) return false;
        if (!text.equals(linkInfo.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = destination.hashCode();
        result = 31 * result + rectangle.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.util.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;

/**
 * Created by iliakan on 11.02.15.
 */
public class LinkInfoExtractor {

    protected PDDocument doc;

    protected int widthTolerance;
    protected List allPages;

    public LinkInfoExtractor(PDDocument doc, int widthTolerance) {
        this.doc = doc;
        this.widthTolerance = widthTolerance;
        this.allPages = doc.getDocumentCatalog().getAllPages();
    }

    public ArrayList<LinkInfo> extractPage(int pageNum) throws IOException {


        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        PDPage page = (PDPage) allPages.get(pageNum);

        List<PDAnnotation> annotations = page.getAnnotations();

        TreeMap<Rectangle2D.Float, PDAnnotation> linksMap = new TreeMap<Rectangle2D.Float, PDAnnotation>(new Comparator<Rectangle2D.Float>() {
            @Override
            public int compare(Rectangle2D.Float o1, Rectangle2D.Float o2) {
                return o1.getMinY() > o2.getMinY() ? 1 : -1;
            }
        });

        System.out.println("Extract page " + pageNum + " annotations size: " + annotations.size());

        for (int j = 0; j < annotations.size(); j++) {
            PDAnnotation annotation = annotations.get(j);

            if (!(annotation instanceof PDAnnotationLink)) continue;

            PDAnnotationLink link = (PDAnnotationLink) annotation;
            PDRectangle rect = link.getRectangle();

            //need to reposition link rectangle to match text space
            float x = rect.getLowerLeftX() - this.widthTolerance;
            float y = rect.getUpperRightY();
            float width = rect.getWidth() + this.widthTolerance;
            float height = rect.getHeight();
            int rotation = page.findRotation();
            if (rotation == 0) {
                PDRectangle pageSize = page.findMediaBox();
                y = pageSize.getHeight() - y;
            } else if (rotation == 90) {
                //do nothing
            }

            Rectangle2D.Float awtRect = new Rectangle2D.Float(x, y, width, height);
            linksMap.put(awtRect, link);

            stripper.addRegion("" + (int)awtRect.getMinY(), awtRect);
        }

        stripper.extractRegions(page);


        ArrayList<LinkInfo> result = new ArrayList<LinkInfo>();

        for(Map.Entry<Rectangle2D.Float, PDAnnotation> entry: linksMap.entrySet()) {
            Rectangle2D.Float rect = entry.getKey();
            PDAnnotation annotation = entry.getValue();

            PDAnnotationLink link = (PDAnnotationLink) annotation;

            String urlText = stripper.getTextForRegion("" + (int)rect.getMinY()).trim();
            PDNamedDestination destination = (PDNamedDestination) link.getDestination();

            result.add(new LinkInfo(destination, rect, urlText));
        }

        return result;

    }

}

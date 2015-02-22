

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.FileOptionHandler;


/**
 * Generates PDF outline from html list
 * 2 levels max depth hardcoded
 *
 * @author Ilya Kantor
 */
public class PdfBookPolisher {
    /**
     * Constructor.
     */
    private PdfBookPolisher() {
        //utility class
    }

    @Option(name="--widthTolerance",usage="Sets a width tolerance to left/right sides of the links for text extraction")
    public int widthTolerance = 10;

    @Option(name="--input", handler=FileOptionHandler.class, usage="Input pdf-file")
    public File inputFile;

    @Option(name="--output", handler=FileOptionHandler.class, usage="Output pdf-file")
    public File outputFile;

    @Option(name="--cover", handler=FileOptionHandler.class, usage="Cover pdf-file")
    public File coverFile = null;

    @Option(name="--pageFrom",usage="The first page to extract ToC")
    public int pageFrom;

    @Option(name="--pageTo",usage="The last page to extract ToC")
    public int pageTo;

    /**
     * This will read in a document and replace all of the urls with
     * http://www.pdfbox.org.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     * @throws Exception If there is an error during the process.
     */
    public static void main(String[] args) throws Exception {

        PdfBookPolisher polisher = new PdfBookPolisher();

        CmdLineParser parser = new CmdLineParser(polisher);


        try {
            parser.parseArgument(args);
            polisher.run();
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }

    }

    protected void run() throws Exception {

        PDDocument doc = PDDocument.load(inputFile);
        PDDocument coverDoc = null;

        if (coverFile != null) {
            coverDoc = PDDocument.load(coverFile);
            PDPage coverPage = coverDoc.getPage(0);

            // as described here
            // http://mail-archives.apache.org/mod_mbox/pdfbox-users/201502.mbox/browser
            doc.importPage(coverPage);
            COSDictionary pages = (COSDictionary) doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PAGES);
            COSArray kids = (COSArray) pages.getDictionaryObject(COSName.KIDS);

            COSBase last = kids.get(kids.size()-1);
            kids.remove(last);
            kids.add(0, last);
        }

        ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();

        LinkInfoExtractor extractor = new LinkInfoExtractor(doc, widthTolerance);

        for (int pageNum = pageFrom; pageNum <= pageTo; pageNum++) {
            linkInfoList.addAll( extractor.extractPage(pageNum) );
        }

        PDDocumentOutline outline = new PDDocumentOutline();

        double firstLevelX = linkInfoList.get(0).getRectangle().getX();

        for(int i = 0; i<linkInfoList.size(); i++) {

            LinkInfo linkInfo = linkInfoList.get(i);

            // add 2nd level of bookmarks
            while (linkInfo.getRectangle().getX() > firstLevelX) {
                PDOutlineItem outlineItem = new PDOutlineItem();
                outlineItem.setTitle(linkInfo.getText());
                outlineItem.setDestination(linkInfo.getDestination());
                outline.getLastChild().addLast(outlineItem);

                if (i == linkInfoList.size()-1) break;

                linkInfo = linkInfoList.get(++i);
            }

            if (i == linkInfoList.size()-1) break;

            PDOutlineItem outlineItem = new PDOutlineItem();
            outlineItem.setTitle(linkInfo.getText());
            outlineItem.setDestination(linkInfo.getDestination());

            outline.addLast(outlineItem);
        }

        doc.getDocumentCatalog().setDocumentOutline(outline);
        doc.save(outputFile);

        // must close after the main doc(!)
        // otherwise error
        if (coverDoc != null) coverDoc.close();

        System.out.println("Done.");
    }

}
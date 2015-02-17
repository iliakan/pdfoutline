

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.exceptions.COSVisitorException;
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

    protected void run() throws IOException, COSVisitorException {

        PDDocument doc = PDDocument.load(inputFile);

        if (coverFile != null) {
            PDDocument cover = PDDocument.load(coverFile);
            doc.getDocumentCatalog().getPages().getKids().add(0, (PDPage) cover.getDocumentCatalog().getAllPages().get(0));
        }

        ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();

        LinkInfoExtractor extractor = new LinkInfoExtractor(doc, widthTolerance);

        for (int pageNum = pageFrom; pageNum <= pageTo; pageNum++) {
            linkInfoList.addAll( extractor.extractPage(pageNum) );
        }

        PDDocumentOutline outline = new PDDocumentOutline();
        doc.getDocumentCatalog().setDocumentOutline(outline);

        double firstLevelX = linkInfoList.get(0).getRectangle().getX();
        for(int i = 0; i<linkInfoList.size(); i++) {

            LinkInfo linkInfo = linkInfoList.get(i);

            // add 2nd level of bookmarks
            while (linkInfo.getRectangle().getX() > firstLevelX) {
                PDOutlineItem outlineItem = new PDOutlineItem();
                outlineItem.setTitle(linkInfo.getText());
                outlineItem.setDestination(linkInfo.getDestination());
                outline.getLastChild().appendChild(outlineItem);

                if (i == linkInfoList.size()-1) break;

                linkInfo = linkInfoList.get(++i);
            }

            if (i == linkInfoList.size()-1) break;

            PDOutlineItem outlineItem = new PDOutlineItem();
            outlineItem.setTitle(linkInfo.getText());
            outlineItem.setDestination(linkInfo.getDestination());

            outline.appendChild(outlineItem);
        }

        doc.save(outputFile);
        System.out.println("Done.");
    }

}
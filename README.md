# PdfOutline

Creates PDF Outline from content TOC.

Meant to be used with HTMLs-saved-as-pdf from Chrome, where first pages contain UL/LI with Table of Contents.

Table of Contents must be links with `#fragments` pointing to chapters.

Arguments example:
```
// convert input.pdf to output.pdf (add TOC), pages 1-3 have the TOC.
input.pdf output.pdf 1 3
```

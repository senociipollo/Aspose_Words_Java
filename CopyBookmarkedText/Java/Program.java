//////////////////////////////////////////////////////////////////////////
// Copyright 2001-2011 Aspose Pty Ltd. All Rights Reserved.
//
// This file is part of Aspose.Words. The source code in this file
// is only intended as a supplement to the documentation, and is provided
// "as is", without warranty of any kind, either expressed or implied.
//////////////////////////////////////////////////////////////////////////
package CopyBookmarkedText;

import java.io.File;
import java.net.URI;

import com.aspose.words.Document;
import com.aspose.words.Bookmark;
import com.aspose.words.CompositeNode;
import com.aspose.words.NodeImporter;
import com.aspose.words.ImportFormatMode;
import com.aspose.words.Paragraph;
import com.aspose.words.Node;


/**
 * Shows how to copy bookmarked text from one document to another while preserving all content and formatting.
 *
 * Does not cover all cases possible (bookmark can start/end in various places of a document
 * making copying scenario more complex).
 *
 * Supported scenarios at the moment are:
 *
 * 1. Bookmark start and end are in the same section of the document, but in different paragraphs.
 * Complete paragraphs are copied.
 */
class Program
{
    /**
     * The main entry point for the application.
     */
    public static void main(String[] args) throws Exception
    {
        URI exeDir = Program.class.getResource("").toURI();
        String dataDir = new File(exeDir.resolve("../../Data")) + File.separator;

        // Load the source document.
        Document srcDoc = new Document(dataDir + "Template.doc");

        // This is the bookmark whose content we want to copy.
        Bookmark srcBookmark = srcDoc.getRange().getBookmarks().get("ntf010145060");

        // We will be adding to this document.
        Document dstDoc = new Document();

        // Let's say we will be appending to the end of the body of the last section.
        CompositeNode dstNode = dstDoc.getLastSection().getBody();

        // It is a good idea to use this import context object because multiple nodes are being imported.
        // If you import multiple times without a single context, it will result in many styles created.
        NodeImporter importer = new NodeImporter(srcDoc, dstDoc, ImportFormatMode.KEEP_SOURCE_FORMATTING);

        // Do it once.
        appendBookmarkedText(importer, srcBookmark, dstNode);

        // Do it one more time for fun.
        appendBookmarkedText(importer, srcBookmark, dstNode);

        // Save the finished document.
        dstDoc.save(dataDir + "Template Out.doc");
    }

    /**
     * Copies content of the bookmark and adds it to the end of the specified node.
     * The destination node can be in a different document.
     *
     * @param importer Maintains the import context.
     * @param srcBookmark The input bookmark.
     * @param dstNode Must be a node that can contain paragraphs (such as a Story).
     */
    private static void appendBookmarkedText(NodeImporter importer, Bookmark srcBookmark, CompositeNode dstNode) throws Exception
    {
        // This is the paragraph that contains the beginning of the bookmark.
        Paragraph startPara = (Paragraph)srcBookmark.getBookmarkStart().getParentNode();

        // This is the paragraph that contains the end of the bookmark.
        Paragraph endPara = (Paragraph)srcBookmark.getBookmarkEnd().getParentNode();

        if ((startPara == null) || (endPara == null))
            throw new IllegalStateException("Parent of the bookmark start or end is not a paragraph, cannot handle this scenario yet.");

        // Limit ourselves to a reasonably simple scenario.
        if (startPara.getParentNode() != endPara.getParentNode())
            throw new IllegalStateException("Start and end paragraphs have different parents, cannot handle this scenario yet.");

        // We want to copy all paragraphs from the start paragraph up to (and including) the end paragraph,
        // therefore the node at which we stop is one after the end paragraph.
        Node endNode = endPara.getNextSibling();

        // This is the loop to go through all paragraph-level nodes in the bookmark.
        for (Node curNode = startPara; curNode != endNode; curNode = curNode.getNextSibling())
        {
            // This creates a copy of the current node and imports it (makes it valid) in the context
            // of the destination document. Importing means adjusting styles and list identifiers correctly.
            Node newNode = importer.importNode(curNode, true);

            // Now we simply append the new node to the destination.
            dstNode.appendChild(newNode);
        }
    }
}

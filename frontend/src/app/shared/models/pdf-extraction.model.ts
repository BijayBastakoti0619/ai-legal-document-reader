export interface ExtractedPage {
  pageNumber: number;
  text: string;
}

export interface PdfExtractionResult {
  pages: ExtractedPage[];
  combinedText: string;
}

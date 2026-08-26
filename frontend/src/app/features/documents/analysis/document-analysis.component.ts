import {
  Component,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

@Component({
  selector: 'app-document-analysis',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl:
    './document-analysis.component.html',
  styleUrl:
    './document-analysis.component.css'
})
export class DocumentAnalysisComponent {

  private readonly route =
    inject(ActivatedRoute);

  readonly documentId =
    this.route.snapshot.paramMap.get('id');
}

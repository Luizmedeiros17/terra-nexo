import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BrandMark } from '../brand-mark/brand-mark';

@Component({
  selector: 'tn-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BrandMark],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer {
  readonly ano = new Date().getFullYear();
}

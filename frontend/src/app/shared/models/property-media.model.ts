/** Um item da galeria de fotos/vídeos de um imóvel (espelha o backend `MediaResponse`). */
export interface PropertyMedia {
  url: string;
  textoAlternativo: string | null;
  ordem: number;
  capa: boolean;
  tipo: 'FOTO' | 'VIDEO';
}

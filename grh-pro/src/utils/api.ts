export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** Normalise une réponse liste paginée ou tableau brut. */
export function unwrapList<T>(data: T[] | PageResponse<T> | null | undefined): T[] {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.content)) return data.content
  return []
}

/** Params par défaut pour récupérer toute la liste admin (évite la troncature à 20). */
export const ALL_PAGE = { page: 0, size: 1000 } as const


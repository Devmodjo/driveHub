/**
 * @author Basile fofack
 * @email juniorbasilefofack@gmail.com
 */
export function dateTransformEnglish(date: String): Date {
  const [day, month, year] = date.split('/');
  const newDate = new Date(+year, +month - 1, +day);
  return newDate;
}

export function formatDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // Les mois commencent à 0
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

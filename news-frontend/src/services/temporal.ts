
export const daysAgoToDate = (daysAgo: number): Date => {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo)
  return date
}

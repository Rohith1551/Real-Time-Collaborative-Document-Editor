/** Computes a single insert or delete between two plain-text strings. */
export function findTextChange(oldText, newText) {
  let start = 0;
  while (
    start < oldText.length &&
    start < newText.length &&
    oldText[start] === newText[start]
  ) {
    start += 1;
  }

  let oldEnd = oldText.length - 1;
  let newEnd = newText.length - 1;

  while (
    oldEnd >= start &&
    newEnd >= start &&
    oldText[oldEnd] === newText[newEnd]
  ) {
    oldEnd -= 1;
    newEnd -= 1;
  }

  return {
    position: start,
    deletedText: oldText.substring(start, oldEnd + 1),
    insertedText: newText.substring(start, newEnd + 1),
  };
}

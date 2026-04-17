const PATTERNS = {
  faculty: /^F\d+$/,
  project: /^PRJ\d+$/,
  inventory: /^INV\d+$/,
  student: /^S\d+$/,
};

export const ID_MESSAGES = {
  faculty: 'Faculty ID must start with F followed by numbers only.',
  project: 'Project ID must start with PRJ followed by numbers only.',
  inventory: 'Inventory ID must start with INV followed by numbers only.',
  student: 'Student ID must start with S followed by numbers only.',
};

export function isValidId(type, value) {
  const pattern = PATTERNS[type];
  return Boolean(pattern && pattern.test((value || '').trim()));
}

export function getIdValidationMessage(type, value) {
  if (isValidId(type, value)) return '';
  return ID_MESSAGES[type] || 'Invalid ID format.';
}

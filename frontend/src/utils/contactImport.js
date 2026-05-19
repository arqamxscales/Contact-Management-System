export function parseContactsCsv(csvText) {
  const lines = csvText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (lines.length === 0) {
    return [];
  }

  const headers = splitCsvLine(lines[0]).map((header) => header.trim().toLowerCase());

  return lines.slice(1).map((line) => {
    const values = splitCsvLine(line);
    const row = {};

    headers.forEach((header, index) => {
      row[header] = values[index]?.trim() ?? "";
    });

    return {
      firstName: pickFirstValue(row, ["firstname", "first_name", "first name"]),
      lastName: pickFirstValue(row, ["lastname", "last_name", "last name"]),
      title: pickFirstValue(row, ["title"]),
      email: pickFirstValue(row, ["email", "emailaddress", "email_address"]),
      phone: pickFirstValue(row, ["phone", "phone_number", "phonenumber"]),
      address: pickFirstValue(row, ["address"])
    };
  });
}

function splitCsvLine(line) {
  const values = [];
  let currentValue = "";
  let inQuotes = false;

  for (let index = 0; index < line.length; index += 1) {
    const character = line[index];

    if (character === '"') {
      inQuotes = !inQuotes;
      continue;
    }

    if (character === "," && !inQuotes) {
      values.push(currentValue);
      currentValue = "";
      continue;
    }

    currentValue += character;
  }

  values.push(currentValue);
  return values;
}

function pickFirstValue(row, keys) {
  for (const key of keys) {
    if (row[key]) {
      return row[key];
    }
  }

  return "";
}
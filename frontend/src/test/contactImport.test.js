import { describe, expect, it } from "vitest";
import { parseContactsCsv } from "../utils/contactImport.js";

describe("parseContactsCsv", () => {
  it("maps common contact columns into contact payloads", () => {
    const rows = parseContactsCsv(`firstName,lastName,title,email,phone,address\nMaya,Chen,Lead,maya@example.com,5551234567,1 Main Street`);

    expect(rows).toEqual([
      {
        firstName: "Maya",
        lastName: "Chen",
        title: "Lead",
        email: "maya@example.com",
        phone: "5551234567",
        address: "1 Main Street"
      }
    ]);
  });
});
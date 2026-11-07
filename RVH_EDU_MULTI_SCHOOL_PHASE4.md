# Multi-school phase 4: backup and restore coverage

The portable database backup now includes school, teacher, teacher-school membership, and teaching-assignment records. For legacy payloads without the three teacher-related tables, restore normalization adds empty arrays. No teacher identity, membership, or assignment is fabricated.

Restore deletion is child-first for assignments and memberships. Inserts follow the table order, with schools and teachers created before memberships and assignments. Existing transaction and integrity checks remain in place.

This is not a claim that all historical backup formats are fully compatible: row-shape changes such as the school ID on enrollment require actual fixture-based restore tests. Android build and device validation remain outstanding.

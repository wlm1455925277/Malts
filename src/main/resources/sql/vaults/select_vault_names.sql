SELECT REPLACE(LOWER(custom_name), ' ', '_') AS normalized_name
FROM malts_vaults
WHERE owner = ?;
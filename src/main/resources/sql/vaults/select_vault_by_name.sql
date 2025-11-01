SELECT *
FROM malts_vaults
WHERE owner = ? AND REPLACE(LOWER(custom_name), ' ', '_') = ?;
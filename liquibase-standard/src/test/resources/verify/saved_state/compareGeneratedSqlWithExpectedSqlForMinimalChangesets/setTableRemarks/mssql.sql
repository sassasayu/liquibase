-- Database: mssql
-- Change Parameter: remarks=A String
-- Change Parameter: tableName=person
IF EXISTS(  SELECT extended_properties.value FROM sys.extended_properties WHERE major_id = OBJECT_ID('dbo.person') AND name = N'MS_DESCRIPTION' AND minor_id = 0 ) BEGIN  EXEC sys.sp_updateextendedproperty @name = N'MS_Description' , @value = N'A String' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'person' END  ELSE  BEGIN  EXEC sys.sp_addextendedproperty @name = N'MS_Description' , @value = N'A String' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'person' END;

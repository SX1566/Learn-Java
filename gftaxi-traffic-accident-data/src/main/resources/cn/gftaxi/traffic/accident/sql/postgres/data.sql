-- 获取汉字拼音首字母的大写
-- 来源：http://blog.qdac.cc/?p=1281
create or replace function cn_first_char(s varchar) returns varchar as $BODY$
  declare
    retval varchar;
    c varchar;
    l integer;
    b bytea;
    w integer;
  begin
  l = length(s);
  retval = '';
  while l > 0 loop
    c = left(s, 1);
    b = convert_to(c, 'GB18030')::bytea;
    if get_byte(b, 0) < 127 then
      retval = retval || upper(c);
    elsif length(b) = 2 then
      begin
      w = get_byte(b,0) * 256 + get_byte(b, 1);
      -- 汉字GBK编码按拼音排序，按字符数来查找
      if w between 48119 and 49061 then    --"J";48119;49061;942
        retval = retval || 'J';
      elsif w between 54481 and 55289 then --"Z";54481;55289;808
        retval = retval || 'Z';
      elsif w between 53689 and 54480 then --"Y";53689;54480;791
        retval = retval || 'Y';
      elsif w between 51446 and 52208 then --"S";51446;52208;762
        retval = retval || 'S';
      elsif w between 52980 and 53640 then --"X";52980;53640;660
        retval = retval || 'X';
      elsif w between 49324 and 49895 then --"L";49324;49895;571
        retval = retval || 'L';
      elsif w between 45761 and 46317 then --"C";45761;46317;556
        retval = retval || 'C';
      elsif w between 45253 and 45760 then --"B";45253;45760;507
        retval = retval || 'B';
      elsif w between 46318 and 46825 then --"D";46318;46825;507
        retval = retval || 'D';
      elsif w between 47614 and 48118 then --"H";47614;48118;504
        retval = retval || 'H';
      elsif w between 50906 and 51386 then --"Q";50906;51386;480
        retval = retval || 'Q';
      elsif w between 52218 and 52697 then --"T";52218;52697;479
        retval = retval || 'T';
      elsif w between 49896 and 50370 then --"M";49896;50370;474
        retval = retval || 'M';
      elsif w between 47297 and 47613 then --"G";47297;47613;316
        retval = retval || 'G';
      elsif w between 47010 and 47296 then --"F";47010;47296;286
        retval = retval || 'F';
      elsif w between 50622 and 50905 then --"P";50622;50905;283
        retval = retval || 'P';
      elsif w between 52698 and 52979 then --"W";52698;52979;281
        retval = retval || 'W';
      elsif w between 49062 and 49323 then --"K";49062;49323;261
        retval = retval || 'K';
      elsif w between 50371 and 50613 then --"N";50371;50613;242
        retval = retval || 'N';
      elsif w between 46826 and 47009 then --"E";46826;47009;183
        retval = retval || 'E';
      elsif w between 51387 and 51445 then --"R";51387;51445;58
        retval = retval || 'R';
      elsif w between 45217 and 45252 then --"A";45217;45252;35
        retval = retval || 'A';
      elsif w between 50614 and 50621 then --"O";50614;50621;7
        retval = retval || 'O';
      end if;
      end;
    end if;
    s = substring(s, 2, l-1);
    l = l-1;
  end loop;
    return retval;
  end;
$BODY$ language plpgsql immutable;
--select cn_first_char('事故性质'); -- SGXZ
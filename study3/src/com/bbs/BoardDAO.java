package com.bbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.util.DBConn;

public class BoardDAO {
	private Connection conn=DBConn.getConnection();
	
	// 데이터 추가
	public int insertBoard(BoardDTO dto) {
		int result=0;
		PreparedStatement pstmt=null;
		StringBuffer sb=new StringBuffer();
		
		try {
			sb.append("INSERT INTO bbs(num, userId, subject, content) ");
			sb.append(" VALUES (bbs_seq.NEXTVAL, ?, ?, ?)");
			
			pstmt=conn.prepareStatement(sb.toString());
			pstmt.setString(1, dto.getUserId());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			
			result=pstmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(pstmt!=null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
		}
		
		return result;
	}
	
	// 데이터 개수
	public int dataCount() {
		int result=0;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String sql;
		
		try {
			sql="SELECT NVL(COUNT(*), 0) FROM bbs";
			pstmt=conn.prepareStatement(sql);
			
			rs=pstmt.executeQuery();
			if(rs.next())
				result=rs.getInt(1);
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return result;
	}

	// 검색에서의 데이터 개수
	public int dataCount(String searchKey, String searchValue) {
		int result=0;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String sql;
		
		try {
			sql="SELECT NVL(COUNT(*), 0)  FROM bbs b JOIN member1 m ON b.userId=m.userId ";
			if(searchKey.equals("userName")) {
				sql+="  WHERE INSTR(userName, ?) = 1 ";
			} else if(searchKey.equals("created")) {
				searchValue=searchValue.replaceAll("-", "");
				sql+="  WHERE TO_CHAR(created, 'YYYYMMDD') = ? ";
			} else {
				sql+="  WHERE INSTR(" + searchKey+ ", ?) >= 1 ";
			}
			
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, searchValue);
			
			rs=pstmt.executeQuery();
			if(rs.next())
				result=rs.getInt(1);
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return result;
	}
	
	// 게시물 리스트
	public List<BoardDTO> listBoard(int start, int end) {
		List<BoardDTO> list=new ArrayList<>();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		
		try {
			sb.append("SELECT * FROM (");
			sb.append("    SELECT ROWNUM rnum, tb.* FROM (");
			sb.append("        SELECT b.num, b.userId, userName, subject");
			sb.append("            ,TO_CHAR(created, 'YYYY-MM-DD') created");
			sb.append("            ,hitCount, NVL(replyCount, 0) replyCount");
			sb.append("            FROM bbs b JOIN member1 m ON b.userId=m.userId  ");
			
			sb.append("            LEFT OUTER JOIN ( ");
			sb.append("                SELECT num, COUNT(*) replyCount FROM bbsReply WHERE answer=0 ");
			sb.append("                GROUP BY num");
			sb.append("            ) c ON b.num = c.num");
			
			sb.append("	       ORDER BY num DESC");
			sb.append("    ) tb WHERE ROWNUM <= ? ");
			sb.append(") WHERE rnum >= ? ");

			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, end);
			pstmt.setInt(2, start);

			rs=pstmt.executeQuery();
			
			while(rs.next()) {
				BoardDTO dto=new BoardDTO();
				
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
				dto.setReplyCount(rs.getInt("replyCount"));
				
				list.add(dto);
			}
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return list;
	}
	
	// 검색에서의 게시물 리스트
	public List<BoardDTO> listBoard(int start, int end, String searchKey, String searchValue) {
		List<BoardDTO> list=new ArrayList<>();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		
		try {
			sb.append("SELECT * FROM (");
			sb.append("    SELECT ROWNUM rnum, tb.* FROM (");
			sb.append("        SELECT b.num, b.userId, userName, subject");
			sb.append("            ,TO_CHAR(created, 'YYYY-MM-DD') created");
			sb.append("            ,hitCount, NVL(replyCount, 0) replyCount");
			sb.append("            FROM bbs b JOIN member1 m ON b.userId=m.userId ");
			
			sb.append("            LEFT OUTER JOIN ( ");
			sb.append("                SELECT num, COUNT(*) replyCount FROM bbsReply WHERE answer=0 ");
			sb.append("                GROUP BY num");
			sb.append("            ) c ON b.num = c.num");
			
			if(searchKey.equals("userName")) {
				sb.append("        WHERE  INSTR(userName, ?) = 1  ");
			} else if(searchKey.equals("created")) {
				searchValue=searchValue.replaceAll("-", "");
				sb.append("        WHERE TO_CHAR(created, 'YYYYMMDD') = ?  ");
			} else {
				sb.append("        WHERE INSTR(" + searchKey + ", ?) >= 1 ");
			}
			sb.append("	       ORDER BY num DESC");
			sb.append("    ) tb WHERE ROWNUM <= ? ");
			sb.append(") WHERE rnum >= ? ");

			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, searchValue);
			pstmt.setInt(2, end);
			pstmt.setInt(3, start);

			rs=pstmt.executeQuery();
			
			while(rs.next()) {
				BoardDTO dto=new BoardDTO();
				
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
				dto.setReplyCount(rs.getInt("replyCount"));
				
				list.add(dto);
			}
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return list;
	}
	
	// 조회수 증가하기
	public int updateHitCount(int num)  {
		int result=0;
		PreparedStatement pstmt=null;
		String sql;
		
		try {
			sql="UPDATE bbs SET hitCount=hitCount+1  WHERE num=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			result=pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return result;
	}
	
	// 해당 게시물 보기
	public BoardDTO readBoard(int num) {
		BoardDTO dto=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		
		try {
			sb.append("SELECT num, b.userId, userName, subject, content");
			sb.append("   ,created, hitCount ");
			sb.append("   FROM bbs b JOIN member1 m ON b.userId=m.userId  ");
			sb.append("   WHERE num = ? ");

			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, num);

			rs=pstmt.executeQuery();
			
			if(rs.next()) {
				dto=new BoardDTO();
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return dto;
	}
	
    // 이전글
    public BoardDTO preReadBoard(int num, String searchKey, String searchValue) {
        BoardDTO dto=null;

        PreparedStatement pstmt=null;
        ResultSet rs=null;
        StringBuffer sb = new StringBuffer();

        try {
            if(searchValue!=null && searchValue.length() != 0) {
                sb.append("SELECT ROWNUM, tb.* FROM ( ");
                sb.append("  SELECT num, subject FROM bbs b JOIN member1 m ON b.userId=m.userId ");
                if(searchKey.equals("userName")) {
                	sb.append("     WHERE (INSTR(userName, ?) = 1)  ");
                } else if(searchKey.equals("created")) {
                	searchValue=searchValue.replaceAll("-", "");
                	sb.append("     WHERE (TO_CHAR(created, 'YYYYMMDD') = ?) ");
                } else {
                	sb.append("     WHERE (INSTR(" + searchKey + ", ?) >= 1) ");
                }
                sb.append("         AND (num > ? ) ");
                sb.append("         ORDER BY num ASC ");
                sb.append("      ) tb WHERE ROWNUM=1 ");

                pstmt=conn.prepareStatement(sb.toString());
                pstmt.setString(1, searchValue);
                pstmt.setInt(2, num);
			} else {
                sb.append("SELECT ROWNUM, tb.* FROM ( ");
                sb.append("  SELECT num, subject FROM bbs b JOIN member1 m ON b.userId=m.userId ");
                sb.append("     WHERE num > ? ");
                sb.append("         ORDER BY num ASC ");
                sb.append("      ) tb WHERE ROWNUM=1 ");

                pstmt=conn.prepareStatement(sb.toString());
                pstmt.setInt(1, num);
			}

            rs=pstmt.executeQuery();

            if(rs.next()) {
                dto=new BoardDTO();
                dto.setNum(rs.getInt("num"));
                dto.setSubject(rs.getString("subject"));
            }
        } catch (Exception e) {
            System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
    
        return dto;
    }

    // 다음글
    public BoardDTO nextReadBoard(int num, String searchKey, String searchValue) {
        BoardDTO dto=null;

        PreparedStatement pstmt=null;
        ResultSet rs=null;
        StringBuffer sb = new StringBuffer();

        try {
            if(searchValue!=null && searchValue.length() != 0) {
                sb.append("SELECT ROWNUM, tb.* FROM ( ");
                sb.append("  SELECT num, subject FROM bbs b JOIN member1 m ON b.userId=m.userId ");
                if(searchKey.equals("userName")) {
                	sb.append("     WHERE (INSTR(userName, ?) = 1)  ");
                } else if(searchKey.equals("created")) {
                	searchValue=searchValue.replaceAll("-", "");
                	sb.append("     WHERE (TO_CHAR(created, 'YYYYMMDD') = ?) ");
                } else {
                	sb.append("     WHERE (INSTR(" + searchKey + ", ?) >= 1) ");
                }
                sb.append("         AND (num < ? ) ");
                sb.append("         ORDER BY num DESC ");
                sb.append(" ) tb WHERE ROWNUM=1 ");

                pstmt=conn.prepareStatement(sb.toString());
                pstmt.setString(1, searchValue);
                pstmt.setInt(2, num);
			} else {
                sb.append("SELECT ROWNUM, tb.* FROM ( ");
                sb.append("    SELECT num, subject FROM bbs b JOIN member1 m ON b.userId=m.userId ");
                sb.append("         WHERE num < ? ");
                sb.append("         ORDER BY num DESC ");
                sb.append(") tb WHERE ROWNUM=1 ");

                pstmt=conn.prepareStatement(sb.toString());
                pstmt.setInt(1, num);
            }

            rs=pstmt.executeQuery();

            if(rs.next()) {
                dto=new BoardDTO();
                dto.setNum(rs.getInt("num"));
                dto.setSubject(rs.getString("subject"));
            }
        } catch (Exception e) {
            System.out.println(e.toString());
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}

        return dto;
    }
	
	// 게시물 수정
	public int updateBoard(BoardDTO dto, String userId) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql;
		
		sql="UPDATE bbs SET subject=?, content=? WHERE num=? AND userId=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getSubject());
			pstmt.setString(2, dto.getContent());
			pstmt.setInt(3, dto.getNum());
			pstmt.setString(4, userId);
			result = pstmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}		
		return result;
	}
	
	// 게시물 삭제
	public int deleteBoard(int num, String userId) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql;
		
		try {
			if(userId.equals("admin")) {
				sql="DELETE FROM bbs WHERE num=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				result = pstmt.executeUpdate();
			}else {
				sql="DELETE FROM bbs WHERE num=? AND userId=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				pstmt.setString(2, userId);
				result = pstmt.executeUpdate();
			}
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}		
		return result;
	}
}

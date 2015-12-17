package br.com.uepb.bsc7.www;
import java.sql.Connection;    
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AcervoDAO {

	Connection conexao = new ConexaoMySQL().getConnection();
	
	//contabiliza o total de livros cadastrados no BD do "siabi"
	public int calculaTotalCadastrados(){
		String sql = "SELECT count(*) FROM acervo_siabi;";
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			rs.next();
			return rs.getInt(1);
		}
		catch (SQLException e){
			e.printStackTrace();
			return 0; 	//verdepoisaquicomofazTEMQUERETORNAR, MAS SERIA MELHOR N�O RETORNAR_ZERO
		}
		
	}
	
	//compara as duas tabelas, siabi/estantes, e retorna o total de encontrados nos dois acervos
	public int calculaTotaItensLocalizados(){
		String sql =  "SELECT count(*) as total from (SELECT acervo_siabi.tombo"
				+ " FROM acervo_estante, acervo_siabi"
				+ " WHERE acervo_estante.cod_barras = acervo_siabi.tombo) as encontrados;";
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			rs.next();
			return rs.getInt(1);
		}
		catch (SQLException e){
			e.printStackTrace();
			return 0; //verjeitocorreto
		}
		
	}
	
	
	//retorna todos dos encontrados nos dois acervos, sem considerar a situa��o.
	public ResultSet selecionarItensLocalizados(){
		String sql = "SELECT ac1.tombo, ac1.titulo, ac1.autor, ac1.localizacao"
				+ " FROM acervo_estante, acervo_siabi ac1"
				+ " WHERE acervo_estante.cod_barras = ac1.tombo;";

		try {
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			while (rs.next()){
				System.out.print(rs.getString(1) + " | ");
				System.out.print(rs.getString(2) + " | ");
				System.out.print(rs.getString(3) + " | ");
				System.out.println(rs.getString(4));
			}
			return rs;
		} catch (SQLException e){
			e.printStackTrace();
			return null;                      //ver qual o melhor retorno 
		}
	}
	
	
	//seleciona os que foram localizados nas estantes, mas "est�o" emprestados
	public ResultSet selecionaLocalizadosMasEmprestados(){
		String sql ="SELECT ac1.tombo, ac1.titulo, ac1.autor, ac1.localizacao"
				+ " FROM acervo_estante, acervo_siabi ac1"
				+ " WHERE (ac1.situacao = '02 - Emprestado'"
				+ " AND ac1.tombo = acervo_estante.cod_barras);";
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			while (rs.next()){
				System.out.print(rs.getString(1) + " | ");
				System.out.print(rs.getString(2) + " | ");
				System.out.print(rs.getString(3) + " | ");
				System.out.println(rs.getString(4));
			}
			return rs;
		} catch (SQLException e){
			e.printStackTrace();
			return null;		//ver retorno
		}
	}
	
	
	
	
	//itens n�o localizados. Considerando apenas os que possuem SITUACAO diferente de EMPRESTADO.
	//estao no siabi, com situa��o diferente de emprestado
	// o mesmo codigo do bd, mas o resultado � diferente. //verificar //possivelmente alterar acrescentando mais ands
	public ResultSet selecionarNaoLocalizados(){
		String sql = "SELECT asi.tombo, asi.situacao, asi.titulo, asi.localizacao, asi.autor"
				+ " FROM acervo_siabi asi"
				+ " where (asi.situacao <> \"02 - Emprestado\""
				+ " AND asi.situacao <>  \"07 - Perdido por Leitor\""
				+ " AND asi.situacao <> \"11 - Extraviado\""
				+ " AND asi.situacao <> \"14 - Inexistente\""
				+ " AND asi.situacao <> \"13 - Exemplar Exclu�do\")"
				+ " AND asi.tombo NOT IN (SELECT cod_barras"
				+ " FROM acervo_estante);"; 

			try {
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			while (rs.next()){
				System.out.print(rs.getString(1) + " | ");
				System.out.print(rs.getString(2) + " | ");
				System.out.print(rs.getString(3) + " | ");
				System.out.print(rs.getString(4) + " | ");
				//System.out.print(rs.getString(5) + " | ");
				System.out.println(rs.getString(5));
			}
			return rs;
		} catch (SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	

	//itens n�o cadastrados
	/******************************************************/
	public ResultSet selecionarN�oCadastrados(){
		String sql = "select seq from acervo_estante2"  
				+ " where cod_barras not in (select tombo from acervo_siabi);";
		
		/*String createTableNaoCadastrados = "Create table nao_cadastrados if not exists("
					+ " ant2 varchar(25),"
					+ " ant1 varchar(25),"
					+ " pos1 varchar(25),"
					+ " pos2 varchar(25),"
					+ " seqlido varchar(25));"; */    
		try {
			this.criarTabelaNaoCadastrados();
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs1 = st.getResultSet();
			rs1.last();					//coloca o "cursor" na �ltima linha do resultSet
			int numeroDeRows = rs1.getRow(); 
			int vizinhos[] = new int[numeroDeRows];
			int i = 0;
			rs1.beforeFirst();			//retorna o cursor pra o inicio do resultSet
			while (rs1.next()){
				vizinhos[i] = rs1.getInt(1);
				//System.out.println(vizinhos[i]);
				i++;
			}
			//int contviz = 0;
			for (int z = 0;z < vizinhos.length; z++){
				
				String tmbs[] = new String[4];
				tmbs = getLivrosVizinhos(vizinhos[z]);
				int nulos = 4 - tmbs.length;
				String posviz[] = new String[4];
				int x = 0;
				while(x < tmbs.length){
					posviz[x] = tmbs[x];
					x++;
				}
				while ((nulos<4) && (nulos != 0)){
					posviz[nulos] = null;
					nulos++;
				}
				String sqlIns = "Insert into nao_cadastrados (ant2, ant1, pos1, pos2, seqlido) values ("+ posviz[0] + ", " + posviz[1] + ", " + posviz[2] + ", " + posviz[3] + ", " + vizinhos[z]+ ");";
				st = conexao.prepareStatement(sqlIns);   
				st.execute();
				//contviz++;
			}
			
			String sql2 = "SELECT * FROM nao_cadastrados";  //14-12  //at� a parte de cima t� funcionando blz.
			PreparedStatement st2 = conexao.prepareStatement(sql2);
			st2.executeQuery(sql2);
			ResultSet rs2 = st2.getResultSet();
			ResultSet localizacao;// = st.getResultSet();
			String [] tombos = new String [4];
			String [] tbs = new String [4];
			while(rs2.next()){
				//String [] teste = new String[4];
				localizacao = null;
				for (int y = 0; y < 4; y++){
					tombos[y] = rs2.getString(y+1);   //L� a linha da tabela de tombos
				}
				for (int z = 0; z < 4; z++){
					String sql3 = "SELECT localizacao FROM acervo_siabi Where tombo = \"" + tombos[z] + "\" ;";
					st = conexao.prepareStatement(sql3);
					st.execute(sql3); 
					localizacao = st2.getResultSet();
					//localizacao.last();
					//localizacao.beforeFirst();
					if (st2.getMaxRows() > 0){  // assim roda, mas ta todo mundo recebendo null while(localizacao != null)
						//localizacao = st2.getResultSet();		//o erro est� na atribui��o dos valores de localizacao
						tbs[z] = localizacao.getString(1); 
						//localizacao.array
						//st.ex
					}			
					else { 
					
						//tinhaumfirsthere
						tbs[z] = "15";
					}
					//String sql4 = "INSERT INTO nao_cadastrados (ant2, ant1, pos1, pos2) values" 
					//		+ " (" + localizacao.getString(1) + ", " + 11 + ", " + 12 + ", " + 13 + ");";
					//st = conexao.prepareStatement(sql4);
					//st.execute(sql4);
					//localizacao = null;
				} 
				String teste[] = new String[1]; teste[0]="98";
				String sql4 = "INSERT INTO testeN (ant2, ant1, pos1, pos2) values" 
						+ " (" + tbs[0] + ", " + tbs[1] + ", " + tbs[2] + ", " + tbs[3] + ");";
				
				st = conexao.prepareStatement(sql4);
				st.execute(sql4);
				
				
			} 
			st = conexao.prepareStatement("SELECT * FROM nao_cadastrados;");
			st.execute();
			ResultSet rsNaoCadastrados = st.getResultSet();   //-14-12 
			/*String sql2 = "Select * from nao_cadastrados";
			PreparedStatement st3 = conexao.prepareStatement(sql2);
			st3.executeQuery(sql2);
			ResultSet rs2 = st3.getResultSet();
			while (rs2.next()){
				st3.executeQuery("select localizacao from acervo_siabi where tombo = " + rs2.getString(1));
				ResultSet rs3 = st3.getResultSet();
				String sqltemp = "Insert into nao_cadastrados (ant2) values (" + rs3.getString(1) + ");";
				st3.executeQuery(sqltemp);
			}*/
			//agora aqui fa�o uma consulta select tudo de nao_cadastrados
			//no rs resultante vou percorrendo com o next() e inserindo a situacao referente
			/*for (int cont = 0; i<vizinhos[1]; cont++){
				selecionarNumSeqVizinhos(vizinhos[cont]){
			}*/		
			return rsNaoCadastrados;   	
		} catch (SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	public void criarTabelaNaoCadastrados(){
		String sql = "Create table nao_cadastrados("
				+ " ant2 varchar(45),"
				+ " ant1 varchar(45),"
				+ " pos1 varchar(45),"
				+ " pos2 varchar(45),"
				+ " seqlido varchar(25));";    /*poderia ser int, mas por via das d�vidas...*/
		try{
			//Statement st = conexao.prepareStatement(createN);
			Statement st = conexao.createStatement();
			st.executeUpdate(sql);
			st.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	
	public void LocalizarVizinhosDeNaoCadastrados(String [] tombosRow){
		
	}
	
	public ResultSet test(){
		try{
			PreparedStatement st = conexao.prepareStatement("Select cod_barras from acervo_estante2  where seq = " + 39 + ";");
			st.execute();
			ResultSet rs = st.getResultSet();
			return rs;
		} catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	/**********************************************************************/
	
	//chamar s� pra um de cada vez; o array vai estar l� no outro metodo. insiro o que for recebendo aqui
	//e depois dou um resultSet. o for vai no metodo anterior
	/**********funcionando*****/
	/*public ResultSet selecionarNumSeqVizinhos(int vizinhos[]){
		try{
			//int cont = 0;
			for (int i = 0; i < vizinhos.length; i++) {
				int cont = 0;
				String sql = "select cod_barras"
						+ " from acervo_estante2"
						+ " where seq = ("+vizinhos[i] + " ) + 1" 
						+ " OR seq = (" + vizinhos[i] + ") + 2"
						+ " OR seq = (" + vizinhos[i] + ") - 2"
						+ " OR seq = (" + vizinhos[i] + ") - 1;";
			
				PreparedStatement st = conexao.prepareStatement(sql);
				st.executeQuery(sql);
				ResultSet rs = st.getResultSet();
				rs.last();
				int numVizinhos = rs.getRow();
				int seqVizinhos[] = new int[numVizinhos];
				rs.beforeFirst();
				while (rs.next()){
				System.out.println(vizinhos[i] + "  " + rs.getInt(1));
				seqVizinhos[cont] = rs.getInt(1);	
				//System.out.println("vizinhosss: " + vizinhos[i]);
				cont++;
				}
				//sytemtavaaqui
			}
			return null;
		} catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}*/  /*****************************************/
	
	public String[] getLivrosVizinhos(int seq){
		String sql = "select cod_barras"
				+ " from acervo_estante2"
				+ " where seq = ("+ seq +") + 1" 
				+ " OR seq = ("+ seq +") + 2"
				+ " OR seq = ("+ seq +") - 2"
				+ " OR seq = ("+ seq +") - 1;";
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.executeQuery(sql);
			ResultSet rs = st.getResultSet();
			rs.last();
			int qtde = rs.getRow();
			String vizinhos[] = new String[qtde];
			rs.beforeFirst();
			int i = 0;
			while (rs.next()){
				vizinhos[i]=rs.getString(1);
				i++;
			}
			return vizinhos;
		} catch (SQLException e){
			e.printStackTrace();
			return null;
		}
	}
		
		
	public static void main(String args[]){
		AcervoDAO acd = new AcervoDAO();
		System.out.println(acd.calculaTotalCadastrados());
		System.out.println(acd.calculaTotaItensLocalizados());
		//acd.selecionarItensLocalizados();
		//acd.selecionaLocalizadosMasEmprestados();
		//acd.selecionarNaoLocalizados();
		//acd.selecionarN�oCadastrados();
		int array[]= new int[5];
		array[0] = 21; array[1] = 22; array [2] = 23;
		array[3] = 29; array[4] =35; 
		//acd.selecionarNumSeqVizinhos(array);
		/* daquiSystem.out.println("Testando o getvizinhos:");
		String t[] = new String [5];
		t= acd.getLivrosVizinhos(1);//(array[4]); aqui*/
		//System.out.println(t[0]);
		//System.out.println(t[1]);
		//System.out.println(t[2]);
		//System.out.println(t[3] + "---");
		ResultSet rs =	acd.selecionarN�oCadastrados(); 
		/*try {
			rs.first();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while (rs.next()){
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  												/*aqui*/
		//acd.criarTabelaNaoCadastrados();
		/*String dois[] = new String[2];
		String um[] = new String[1];
		System.out.println(dois.length);
		dois = um;
		System.out.println(um.length);
		System.out.println(dois.length);*/
		ResultSet t = acd.test();
		try {
			t.last();
			if (t.getRow() > 0){
				t.first(); System.out.println(t.getString(1));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*try {
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(t.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
}

/*
 * 21
22
23
29
35
*/


/********
 * encontrados no banco de dados do siabi que estamos usando como teste.
 ********
 ********
 * 01 - Dispon�vel
 * 02 - Emprestado
 * 03 - 
 * 04 - Em Processos T�cnicos
 * 05 - 
 * 06 -
 * 07 - Perdido por Leitor
 * 08 - Fora de Empr�stimos v
 * 09 - 
 * 10 - Disp.Emp.Especial
 * 11 - Extraviado
 * 12 - Convertido
 * 13 - Exemplar Exclu�do
 * 14 - Inexistente
 * 15 - Encadernador
 * 16 - ... 30+ . sem nada.
 */



/***********jeito que estava a consulta
String sql = "select cod_barras"
+ " from acervo_estante2"
+ " where seq = (select seq from acervo_estante2 where tombo = " + vizinhos[i] + " ) + 1" 
+ " OR seq = (select seq from acervo_siabi where tombo = " + vizinhos[i] + ") + 2"
+ " OR seq = (select seq from acervo_siabi where tombo = " + vizinhos[i] + ") - 2"
+ " OR seq = (select seq from acervo_siabi where tombo = " + vizinhos[i] + ") - 1;";
*/





/*========================================ultimo====================================================
public ResultSet selecionarN�oCadastrados(){
String sql = "select seq from acervo_estante2"  
		+ " where cod_barras not in (select tombo from acervo_siabi);";

/*String createTableNaoCadastrados = "Create table nao_cadastrados if not exists("
			+ " ant2 varchar(25),"
			+ " ant1 varchar(25),"
			+ " pos1 varchar(25),"
			+ " pos2 varchar(25),"
			+ " seqlido varchar(25));"; asterisco/    
try {
	this.criarTabelaNaoCadastrados();
	PreparedStatement st = conexao.prepareStatement(sql);
	st.executeQuery(sql);
	ResultSet rs1 = st.getResultSet();
	rs1.last();					//coloca o "cursor" na �ltima linha do resultSet
	int numeroDeRows = rs1.getRow(); 
	int vizinhos[] = new int[numeroDeRows];
	int i = 0;
	rs1.beforeFirst();			//retorna o cursor pra o inicio do resultSet
	while (rs1.next()){
		vizinhos[i] = rs1.getInt(1);
		//System.out.println(vizinhos[i]);
		i++;
	}
	//int contviz = 0;
	for (int z = 0;z < vizinhos.length; z++){
		
		String tmbs[] = new String[4];
		tmbs = getLivrosVizinhos(vizinhos[z]);
		int nulos = 4 - tmbs.length;
		String posviz[] = new String[4];
		int x = 0;
		while(x < tmbs.length){
			posviz[x] = tmbs[x];
			x++;
		}
		while ((nulos<4) && (nulos != 0)){
			posviz[nulos] = null;
			nulos++;
		}
		String sqlIns = "Insert into nao_cadastrados (ant2, ant1, pos1, pos2, seqlido) values ("+ posviz[0] + ", " + posviz[1] + ", " + posviz[2] + ", " + posviz[3] + ", " + vizinhos[z]+ ");";
		st = conexao.prepareStatement(sqlIns);   
		st.execute();
		//contviz++;
	}
	String sql2 = "SELECT * FROM nao_cadastrados";  //14-12  //at� a parte de cima t� funcionando blz.
	st = conexao.prepareStatement(sql2);						//1612 altereiaquicriando um st2
	st.executeQuery(sql2);
	ResultSet rs2 = st.getResultSet();
	ResultSet localizacao;// = st.getResultSet();
	String [] tombos = new String [4];
	while(rs2.next()){
		//String [] teste = new String[4];
		for (int y = 0; y < 4; y++){
			tombos[y] = rs2.getString(y+1);   //L� a linha da tabela de tombos
		}
		for (int z = 0; z < 4; z++){
			String sql3 = "SELECT localizacao FROM acervo_siabi Where tombo = \"" + tombos[z] + "\" ;";
			st = conexao.prepareStatement(sql3);
			//st.execute(sql3); 
			//localizacao = st.getResultSet();
			//localizacao.last();
			if (st.execute(sql3)){
				localizacao = st.getResultSet();
				tombos[z] = localizacao.getString(1); 
				//localizacao.array
				//st.ex
			}
			else { 
			
				//tinhaumfirsthere
				tombos[z] = "---";
			}
			localizacao = null;
		} 
		//String teste[] = new String[1]; teste[0]="98";
		
		String sql4 = "INSERT INTO nao_cadastrados (ant2, ant1, pos1, pos2) values" 
				+ " (" + tombos[0] + ", " + 11 + ", " + 12 + ", " + 13 + ");";
		st = conexao.prepareStatement(sql4);
		st.execute(sql4);
		
		
	}
	st = conexao.prepareStatement("SELECT * FROM nao_cadastrados;");
	st.execute();
	ResultSet rsNaoCadastrados = st.getResultSet();   //-14-12 
			
	return rsNaoCadastrados;   	
} catch (SQLException e){
	e.printStackTrace();
	return null;
}
}
***********************==========================================*/
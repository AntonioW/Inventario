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
			return 0; 	//verdepoisaquicomofazTEMQUERETORNAR, MAS SERIA MELHOR NÃO RETORNAR_ZERO
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
	
	
	//retorna todos dos encontrados nos dois acervos, sem considerar a situação.
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
	
	
	//seleciona os que foram localizados nas estantes, mas "estão" emprestados
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
	
	
	
	
	//itens não localizados. Considerando apenas os que possuem SITUACAO diferente de EMPRESTADO.
	//estao no siabi, com situação diferente de emprestado
	// o mesmo codigo do bd, mas o resultado é diferente. //verificar //possivelmente alterar acrescentando mais ands
	public ResultSet selecionarNaoLocalizados(){
		String sql = "SELECT asi.tombo, asi.situacao, asi.titulo, asi.localizacao, asi.autor"
				+ " FROM acervo_siabi asi"
				+ " where (asi.situacao <> \"02 - Emprestado\""
				+ " AND asi.situacao <>  \"07 - Perdido por Leitor\""
				+ " AND asi.situacao <> \"11 - Extraviado\""
				+ " AND asi.situacao <> \"14 - Inexistente\""
				+ " AND asi.situacao <> \"13 - Exemplar Excluído\")"
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
	
	

	//itens não cadastrados
	/******************************************************/
	public ResultSet selecionarNãoCadastrados(){
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
			rs1.last();					//coloca o "cursor" na última linha do resultSet
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
			
			String sql2 = "SELECT * FROM nao_cadastrados";  //14-12  //até a parte de cima tá funcionando blz.
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
					tombos[y] = rs2.getString(y+1);   //Lê a linha da tabela de tombos
				}
				for (int z = 0; z < 4; z++){
					String sql3 = "SELECT localizacao FROM acervo_siabi Where tombo = \"" + tombos[z] + "\" ;";
					st = conexao.prepareStatement(sql3);
					st.execute(sql3); 
					localizacao = st2.getResultSet();
					//localizacao.last();
					//localizacao.beforeFirst();
					if (st2.getMaxRows() > 0){  // assim roda, mas ta todo mundo recebendo null while(localizacao != null)
						//localizacao = st2.getResultSet();		//o erro está na atribuição dos valores de localizacao
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
			//agora aqui faço uma consulta select tudo de nao_cadastrados
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
				+ " seqlido varchar(25));";    /*poderia ser int, mas por via das dúvidas...*/
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
	
	//chamar só pra um de cada vez; o array vai estar lá no outro metodo. insiro o que for recebendo aqui
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
	
	
	
	/***********QuintaFeira*************************/
	/***********tentandoEncontrarLocalizacaotombosvizinhos*****************/
	public String[] SelecionarLocalicaoDosVizinhos(int seq){
		String [] temp =  this.getLivrosVizinhos(seq);
		/*
		for (int i = 0; i < temp.length; i++){
			
			System.out.println(temp[i]);
			
		}*/
		
		return temp;
	}
	
	
	
	@SuppressWarnings("null")
	public String[] getLocalizacaoLivrosVizinhos(String [] vizinhos){
		String [] localizacoes = null;
		String sql = "select localizacao" 
				+ " from (select tombo, localizacao" 
				+ " from acervo_siabi, acervo_estante2"
				+ " where acervo_estante2.cod_barras = acervo_siabi.tombo) as tomboloc"
				+ " where tombo = ";
		ResultSet rs;
		try{
			
			for (int i = 0; i < vizinhos.length; i++) {
				PreparedStatement st = conexao.prepareStatement(sql + vizinhos[i] + ";");
				st.execute();
				rs = st.getResultSet();
				//if(rs.isLast()){
				//localizacoes[i] = rs.getString(1);
				rs.beforeFirst();
				while(rs.next()){
					localizacoes[i] = rs.getString(1);
					System.out.println(rs.getString(1));
				}
				/*else {
					localizacoes[i] = null; 
				}*/
			}
			return localizacoes;
			
		} catch (SQLException e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	//tápegandosóostombos
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
		
	
	/*
	 * possivel utilização no selecionarlocalizacaovizinhos2
	 * evitando que se localize os vizinhos de numeros de sequencia que não estao na tabela
	 */ 
	 
	public int CalcularTamanhoDeUmaTabela(String NomeTabela){
		String sql = "Select count(*) as total"
				+ " from " + NomeTabela + ";";
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.execute();
			ResultSet rs = st.getResultSet();
			rs.beforeFirst();
			if (rs.next()){
			return rs.getInt(1);
			} else return -1;
			
		}catch (SQLException e){
			e.printStackTrace();
			return -1;
		}
		
	}
	
	
	//RETORNA AS CDDs dos vizinhos, no entanto, pode apresentar um valor para seqs menores ou maiores que o numero total de rows
	public ResultSet getLocalizacaoDosVizinhos2(int seq){
		String sql = "SELECT localizacao" 
				+ " FROM (select cod_barras"
				+ " from acervo_estante2"
				+ " where ((seq = " + seq + " + 1)"
				+ " OR (seq =  " + seq + " + 2)" 
				+ " OR (seq =  " + seq + " - 2)" 
				+ " OR (seq =  " + seq + " - 1))) as vizinhos, acervo_siabi" 
				+ " WHERE cod_barras = tombo;";
		try {
			PreparedStatement st = conexao.prepareStatement(sql);
			st.execute(sql); 
			ResultSet rs = st.getResultSet();
			return rs;
			
		} catch (SQLException e){
			e.printStackTrace();
			return null;
		}
	
	}
	
	
	/*ultrapassado*/
	public ResultSet SelecionarTresUltimasLinhas(String NomeTabela){
		int tamanho = this.CalcularTamanhoDeUmaTabela(NomeTabela);
		String sql = "SELECT * from " + NomeTabela + ";";
		String [] ultimos = new String [3];
		int i = 1;
		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.execute(sql);
			ResultSet rs = st.getResultSet();
			int z = 0;
			while (rs.next()){
				i++;
				if (i > (tamanho - 3)){
					//ultimos[z] = rs.getString(1);
					System.out.println("tam: " + tamanho);
					z++;
				}
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	//assume que todas as tabelas passadas terão um campo sequência
	public ResultSet getTresUltimasLinhas(String nome_tabela){
		String sql = "SELECT *"
				+ " FROM (select count(*) as ult"
				+ " from " + nome_tabela + ") as ultimo, " 
				+ nome_tabela + " where seq > (ult - 3);"; 

		try{
			PreparedStatement st = conexao.prepareStatement(sql);
			st.execute(sql);
			ResultSet rs = st.getResultSet();
			return rs;
		}catch (SQLException e){
			e.printStackTrace();
			return null;
		}

	}
	
	public ResultSet selecionarItensParaVerificacao(){
		String sql = "select *" 
				+ " from acervo_estante"
				+ " where verificar is not null;";
		try{
			Statement st = conexao.prepareStatement(sql);
			st.execute(sql);
			ResultSet rs = st.getResultSet();
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	
	}
	
	//22-12-noite
	public void inserirLinha(String cod_barras, String verif){
    	int numeroSequencia_ultimo = 0;
    	String sql2 = "Select count(*) as total from acervo_estante2";
		
		try{
			Statement st = conexao.prepareStatement(sql2);
			st.execute(sql2);
			ResultSet rs = st.getResultSet();
			rs.last();
			numeroSequencia_ultimo = rs.getInt(1);
			numeroSequencia_ultimo++;
			String sql = "Insert into acervo_estante2 (seq, cod_barras, verificar) values"
	    			+ " (" + numeroSequencia_ultimo + ", " + cod_barras + ", \"" + verif + "\");";
			st = conexao.prepareStatement(sql);
			st.execute(sql);
			
		}catch(SQLException e){
			e.printStackTrace();
		}
    	
    }
	
	
	public void removerUltimaLinha(){
		String sql = "delete from acervo_estante2 where ("
				+ " select seq from (select count(*) ultimo from acervo_estante2) as tamanho where seq = ultimo);";
		try {
			Statement st = conexao.prepareStatement(sql);
			st.execute(sql);
		} catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		AcervoDAO acd = new AcervoDAO();
		System.out.println(acd.calculaTotalCadastrados());
		System.out.println(acd.calculaTotaItensLocalizados());
		//acd.selecionarItensLocalizados();
		//acd.selecionaLocalizadosMasEmprestados();
		//acd.selecionarNaoLocalizados();
		//acd.selecionarNãoCadastrados();
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
		//String [] tombs = acd.SelecionarLocalicaoDosVizinhos(1);
		//System.out.println(tombs[0] +" - "+ tombs[1]);// + " - " +  tombs[2] + " - " + tombs[3]);
		//String [] locs = acd.getLocalizacaoLivrosVizinhos(tombs);
		ResultSet rs = acd.getLocalizacaoDosVizinhos2(2);
		try {
			while (rs.next()){
				System.out.println(rs.getString(1) + " - ");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Total: " + acd.CalcularTamanhoDeUmaTabela("acervo_siabi") );
		//rs = acd.SelecionarTresUltimasLinhas("acervo_siabi");
		//rs = acd.getTresUltimasLinhas("acervo_estante2");
		rs = acd.selecionarItensParaVerificacao();
		try {
			//rs.beforeFirst();
			while (rs.next()){
				System.out.print(rs.getString(1) + " ");
				System.out.println(rs.getString(2) + " ");
				//System.out.println(rs.getString(3)+ " ");
				/*System.out.print(rs.getString(4)+ " ");
				System.out.print(rs.getString(5)+ " ");
				System.out.print(rs.getString(6)+ " ");
				System.out.print(rs.getString(7)+ " ");
				System.out.print(rs.getString(8)+ " ");
				System.out.println(rs.getString(9));*/
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		acd.inserirLinha("221221015", "kkk4");
		//acd.removerUltimaLinha();
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
 * 01 - Disponível
 * 02 - Emprestado
 * 03 - 
 * 04 - Em Processos Técnicos
 * 05 - 
 * 06 -
 * 07 - Perdido por Leitor
 * 08 - Fora de Empréstimos v
 * 09 - 
 * 10 - Disp.Emp.Especial
 * 11 - Extraviado
 * 12 - Convertido
 * 13 - Exemplar Excluído
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
public ResultSet selecionarNãoCadastrados(){
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
	rs1.last();					//coloca o "cursor" na última linha do resultSet
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
	String sql2 = "SELECT * FROM nao_cadastrados";  //14-12  //até a parte de cima tá funcionando blz.
	st = conexao.prepareStatement(sql2);						//1612 altereiaquicriando um st2
	st.executeQuery(sql2);
	ResultSet rs2 = st.getResultSet();
	ResultSet localizacao;// = st.getResultSet();
	String [] tombos = new String [4];
	while(rs2.next()){
		//String [] teste = new String[4];
		for (int y = 0; y < 4; y++){
			tombos[y] = rs2.getString(y+1);   //Lê a linha da tabela de tombos
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
package br.com.uepb.bsc7.www;

public class Estatistica {

	private int totalAcervoSiabi;
	private int totalAcervoEstantes;
	private int totalItensLocalizados;
	private int totalItensNaoLocalizados; //estãoCadastradosno siabi, mas não tiveram o cod_barras lido
	private int totalEmprestados;
	private int totalDisponiveis;
	private int totalDispEmpEspecial;
	public int getTotalAcervoSiabi() {
		return totalAcervoSiabi;
	}
	public void setTotalAcervoSiabi(int totalAcervoSiabi) {
		this.totalAcervoSiabi = totalAcervoSiabi;
	}
	public int getTotalAcervoEstantes() {
		return totalAcervoEstantes;
	}
	public void setTotalAcervoEstantes(int totalAcervoEstantes) {
		this.totalAcervoEstantes = totalAcervoEstantes;
	}
	public int getTotalItensLocalizados() {
		return totalItensLocalizados;
	}
	public void setTotalItensLocalizados(int totalItensLocalizados) {
		this.totalItensLocalizados = totalItensLocalizados;
	}
	public int getTotalItensNaoLocalizados() {
		return totalItensNaoLocalizados;
	}
	public void setTotalItensNaoLocalizados(int totalItensNaoLocalizados) {
		this.totalItensNaoLocalizados = totalItensNaoLocalizados;
	}
	public int getTotalEmprestados() {
		return totalEmprestados;
	}
	public void setTotalEmprestados(int totalEmprestados) {
		this.totalEmprestados = totalEmprestados;
	}
	public int getTotalDisponiveis() {
		return totalDisponiveis;
	}
	public void setTotalDisponiveis(int totalDisponiveis) {
		this.totalDisponiveis = totalDisponiveis;
	}
	public int getTotalDispEmpEspecial() {
		return totalDispEmpEspecial;
	}
	public void setTotalDispEmpEspecial(int totalDispEmpEspecial) {
		this.totalDispEmpEspecial = totalDispEmpEspecial;
	}
	
	public static void main(String args[]){
		Estatistica est = new Estatistica();
		est.setTotalAcervoEstantes(2147000000);
		System.out.println(est.getTotalAcervoEstantes());
	}
	
}

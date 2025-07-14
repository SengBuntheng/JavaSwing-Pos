package globalValues;

import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModelProduct extends AbstractTableModel {
	private final ArrayList<OrderDetails> listOrderDetails;
	private final String[] columnNames = {
			"Item Code",
			"Description",
			"Quantity",
			"Unit Price",
			"Discount",
			"Extended Price"
	};

	public TableModelProduct() {
		this.listOrderDetails = new ArrayList<>();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public void addOrderDetail(OrderDetails orderDetail) {
		if (orderDetail == null) return;
		listOrderDetails.add(orderDetail);
		int newIndex = listOrderDetails.size() - 1;
		fireTableRowsInserted(newIndex, newIndex);
	}

	public void removeOrderDetail(int index) {
		if (index < 0 || index >= listOrderDetails.size()) return;
		listOrderDetails.remove(index);
		fireTableRowsDeleted(index, index);
	}

	public void removeAll() {
		int size = listOrderDetails.size();
		if (size > 0) {
			listOrderDetails.clear();
			fireTableRowsDeleted(0, size - 1);
		}
	}

	public void editQuantity(int index, int quantity) {
		if (index < 0 || index >= listOrderDetails.size()) return;
		listOrderDetails.get(index).setQuantity(quantity);
		fireTableRowsUpdated(index, index);
	}

	public void editPrice(int index, BigDecimal price) {
		if (index < 0 || index >= listOrderDetails.size() || price == null) return;
		listOrderDetails.get(index).setUnitPrice(price);
		fireTableRowsUpdated(index, index);
	}

	public void applyDiscount(int index, BigDecimal discount) {
		if (index < 0 || index >= listOrderDetails.size() || discount == null) return;
		listOrderDetails.get(index).setDiscount(discount);
		fireTableRowsUpdated(index, index);
	}

	public BigDecimal calculateSubtotal() {
		BigDecimal subTotal = BigDecimal.ZERO;
		for (OrderDetails od : listOrderDetails) {
			BigDecimal extPrice = od.getExtPrice();
			if (extPrice != null) {
				subTotal = subTotal.add(extPrice);
			}
		}
		return subTotal;
	}

	public BigDecimal calculateTotalDiscount() {
		BigDecimal totalDiscount = BigDecimal.ZERO;
		for (OrderDetails od : listOrderDetails) {
			BigDecimal discount = od.getDiscount();
			if (discount != null) {
				totalDiscount = totalDiscount.add(discount);
			}
		}
		return totalDiscount;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return listOrderDetails.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= listOrderDetails.size()) return null;
		OrderDetails od = listOrderDetails.get(rowIndex);
		switch (columnIndex) {
			case 0: return od.getProductID();
			case 1: return od.getProductName();
			case 2: return od.getQuantity();
			case 3: return od.getUnitPrice();
			case 4: return od.getDiscount();
			case 5: return od.getExtPrice();
			default: return null;
		}
	}
}

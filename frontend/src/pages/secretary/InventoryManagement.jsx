import { useState, useEffect } from 'react';
import { getInventory, addInventoryItem, updateInventoryItem, disposeInventoryItem } from '../../api/api';

const CONDITIONS = ['new', 'good', 'fair', 'poor', 'disposed'];
const EMPTY = { item_id: '', item_name: '', category: '', quantity: 1, location: '', condition: 'good', is_lab_item: false };

export default function InventoryManagement({ departmentId, isTechnical }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [filter, setFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try { setItems((await getInventory(departmentId)).data || []); }
    catch { setError('Failed to load inventory.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [departmentId]);

  const handleSave = async () => {
    setError('');
    try {
      if (modal === 'add') await addInventoryItem({ ...form, quantity: parseInt(form.quantity) }, departmentId, isTechnical);
      else await updateInventoryItem(form.item_id, form);
      setSuccess(modal === 'add' ? 'Item added.' : 'Item updated.');
      setModal(null); load();
    } catch (e) { setError(e.response?.data?.error || 'Save failed.'); }
  };

  const handleDispose = async (itemId) => {
    if (!confirm('Mark item as disposed?')) return;
    try { await disposeInventoryItem(itemId); setSuccess('Item disposed.'); load(); }
    catch (e) { setError(e.response?.data?.error || 'Failed.'); }
  };

  const filtered = filter ? items.filter(i => i.item_name?.toLowerCase().includes(filter.toLowerCase()) || i.category?.toLowerCase().includes(filter.toLowerCase())) : items;

  return (
    <div>
      <div className="section-header">
        <h3>Inventory Management — UC-08</h3>
        <button className="btn btn-primary" onClick={() => { setForm({ ...EMPTY }); setError(''); setModal('add'); }}>+ Add Item</button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div style={{ marginBottom: 16 }}>
        <input className="search-bar" type="text" placeholder="Search by name or category..." value={filter} onChange={e => setFilter(e.target.value)} />
      </div>

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Qty</th><th>Location</th><th>Condition</th><th>Lab Item</th><th>Actions</th></tr></thead>
              <tbody>
                {filtered.length === 0 ? <tr><td colSpan={8}><div className="empty-state">No inventory items.</div></td></tr>
                  : filtered.map(i => (
                    <tr key={i.item_id}>
                      <td>{i.item_id}</td>
                      <td>{i.item_name}</td>
                      <td>{i.category}</td>
                      <td>{i.quantity}</td>
                      <td>{i.location || '—'}</td>
                      <td><span className={`badge ${i.condition === 'disposed' ? 'badge-danger' : i.condition === 'new' || i.condition === 'good' ? 'badge-success' : 'badge-warning'}`}>{i.condition}</span></td>
                      <td>{i.is_lab_item ? 'Yes' : 'No'}</td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-secondary btn-sm" onClick={() => { setForm({ ...i }); setError(''); setModal('edit'); }}>Edit</button>
                          {i.condition !== 'disposed' && <button className="btn btn-danger btn-sm" onClick={() => handleDispose(i.item_id)}>Dispose</button>}
                        </div>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">{modal === 'add' ? 'Add Inventory Item' : 'Edit Item'}</div>
            {error && <div className="alert alert-error">{error}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>Item ID *</label>
                <input value={form.item_id} onChange={e => setForm(p => ({ ...p, item_id: e.target.value }))} disabled={modal === 'edit'} />
              </div>
              <div className="form-group">
                <label>Item Name *</label>
                <input value={form.item_name} onChange={e => setForm(p => ({ ...p, item_name: e.target.value }))} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Category *</label>
                <input value={form.category} onChange={e => setForm(p => ({ ...p, category: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>Quantity *</label>
                <input type="number" value={form.quantity} onChange={e => setForm(p => ({ ...p, quantity: e.target.value }))} min="1" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Location</label>
                <input value={form.location || ''} onChange={e => setForm(p => ({ ...p, location: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>Condition</label>
                <select value={form.condition} onChange={e => setForm(p => ({ ...p, condition: e.target.value }))}>
                  {CONDITIONS.map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
            </div>
            {isTechnical && (
              <div className="form-group">
                <label><input type="checkbox" checked={form.is_lab_item} onChange={e => setForm(p => ({ ...p, is_lab_item: e.target.checked }))} style={{ width: 'auto', marginRight: 8 }} />Lab Item</label>
              </div>
            )}
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

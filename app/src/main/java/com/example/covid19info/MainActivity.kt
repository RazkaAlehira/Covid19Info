package com.example.covid19info

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adapter.ListCountryAdapter
import com.example.pojo.CountriesItem
import com.example.pojo.Global
import com.example.retrofit.CovidInterface
import com.example.retrofit.RetrofitService
import id.idn.fahru.covid19info.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // rvAdapter dibuat variabel Global
    private lateinit var rvAdapter : ListCountryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflater dan inflate binding mesti ada
        val inflater = layoutInflater
        binding = ActivityMainBinding.inflate(inflater)

        // ganti setContentView dengan binding.root
        setContentView(binding.root)

        // definisikan recyclerview adapter
        rvAdapter = ListCountryAdapter()

        // setting recyclerview
        binding.rvCountry.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            // gunakan recyclerview adapter yang telah didefinisikan sebelumnya
            adapter = rvAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            // jalankan kembali fungsi getCovidData untuk merefresh data sebelumnya.
            getCovidData(binding)
        }

        // atur swiperefresh
        getCovidData(binding)
        binding.swipeRefresh.setOnRefreshListener {
            // jalankan kembali fungsi getCovidData untuk merefresh data sebelumnya.
            getCovidData(binding)
        }

        // atur searchView START
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // bagian onQueryTextSubmit ini berjalan hanya ketika tombol search diklik
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // bagian onQueryTextChange ini berjalan ketika teks diganti
                    rvAdapter.filter.filter(newText)
                    Log.e("TestSearchView", newText.toString())
                    return false
                }
            }
        )
        // atur SearchView END
    }

    // buat fungsi getCovidData
    private fun getCovidData(binding: ActivityMainBinding) {
        // buat lifecyclescope untuk mengakses retrofit
        lifecycleScope.launch {
            // definisikan retrofit service berdasarkan interface yang dituju
            val retrofit = RetrofitService.buildService(CovidInterface::class.java)
            // definisikan variabel summary (sesuaikan aja namanya)
            val summary = retrofit.getSummary()
            if (summary.isSuccessful) { // jika berhasil
                // buat variabel dataCountry yang berisi list countries dari API
                val dataCountry = summary.body()?.countries as List<*>

                // buat variabel yang memuat nilai global dari API
                val dataGlobal = summary.body()?.global as Global
                // Masukkan data ke dalam activity main
                binding.run {
                    txtConfirmedGlode.text = dataGlobal.totalConfirmed.toString()
                    txtRecoveredGlobe.text = dataGlobal.totalRecovered.toString()
                    txtDeathsGlode.text = dataGlobal.totalDeaths.toString()
                }

                // hilangkan progressbar
                binding.progressBar.visibility = View.GONE

                // hilangkan loading swiperrefresh
                binding.swipeRefresh.isRefreshing = false

                // tambahkan ke dalam rvAdapter
                rvAdapter.addData(dataCountry as List<CountriesItem>)
            } else {
                Log.e("RetrofitFailed", summary.errorBody().toString())
            }
        }
    }
}

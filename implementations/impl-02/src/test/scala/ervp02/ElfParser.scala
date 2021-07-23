// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import java.io._
import scodec._
import scodec.bits._
import scodec.codecs._

import scala.collection.mutable.ListBuffer

object ElfParser {

  private val ELF_HEADER_MAG: Int = 0x464c457f
  private val ELF_HEADER_MACHINE_RISCV: Int = 0xf3
  private val ELF_HEADER_IDENT_CLASS_32B: Int = 1
  private val PT_LOAD: Int = 1

  case class ElfHeader(
      e_ident_mag: Long,
      e_ident_class: Int,
      e_ident_data: Int,
      e_ident_version: Int,
      e_ident_abi: Int,
      e_ident_abiversion: Int,
      e_ident_pad: ByteVector,
      e_type: Int,
      e_machine: Int,
      e_version: Long,
      e_entry: Long,
      e_phoff: Long,
      e_shoff: Long,
      e_flags: Long,
      e_ehsize: Int,
      e_phentsize: Int,
      e_phnum: Int,
      e_shentsize: Int,
      e_shnum: Int,
      e_shstrndx: Int
  )

  implicit val elfHeader = {
    ("e_ident_mag" | uint32L) ::
      ("e_ident_class" | uint8) ::
      ("e_ident_data" | uint8) ::
      ("e_ident_version" | uint8) ::
      ("e_ident_abi" | uint8) ::
      ("e_ident_abiversion" | uint8) ::
      ("e_ident_pad" | bytes(7)) ::
      ("e_type" | uint16L) ::
      ("e_machine" | uint16L) ::
      ("e_version" | uint32L) ::
      ("e_entry" | uint32L) ::
      ("e_phoff" | uint32L) ::
      ("e_shoff" | uint32L) ::
      ("e_flags" | uint32L) ::
      ("e_ehsize" | uint16L) ::
      ("e_phentsize" | uint16L) ::
      ("e_phnum" | uint16L) ::
      ("e_shentsize" | uint16L) ::
      ("e_shnum" | uint16L) ::
      ("e_shstrndx" | uint16L)
  }.as[ElfHeader]

  case class PhEntry(
      p_type: Long,
      p_offset: Long,
      p_vaddr: Long,
      p_paddr: Long,
      p_filesz: Long,
      p_memsz: Long,
      p_flags: Long,
      p_align: Long
  )

  implicit val phEntry = {
    ("p_type" | uint32L) ::
      ("p_offset" | uint32L) ::
      ("p_vaddr" | uint32L) ::
      ("p_paddr" | uint32L) ::
      ("p_filesz" | uint32L) ::
      ("p_memsz" | uint32L) ::
      ("p_flags" | uint32L) ::
      ("p_align" | uint32L)
  }.as[PhEntry]

  case class MemSection(
      ph_entry: PhEntry,
      mem: Array[Byte]
  )

  private def _get_ph_hdrs(elf: FileInputStream, hdr: ElfHeader): Seq[PhEntry] = {
    val ph_entries = ListBuffer[PhEntry]()

    val ph_entry_len: Int = phEntry.sizeBound.exact.get.toInt / 8
    assert(hdr.e_phentsize == ph_entry_len)
    val skipped = elf.skip(hdr.e_phoff)

    for (_ <- 0 until hdr.e_phnum) {
      val entry_raw: Array[Byte] = elf.readNBytes(ph_entry_len)
      val entry: PhEntry = phEntry.decode(ByteVector.view(entry_raw).toBitVector).require.value
      ph_entries += entry
    }

    // a hackish workaround for a lack of `lseek(SEEK_SET)`
    elf.skip(-skipped)
    elf.skip(-ph_entry_len * hdr.e_phnum)

    ph_entries.toList
  }

  def parse(filename: String): Seq[MemSection] = {
    val mem_secs = ListBuffer[MemSection]()
    val elf: FileInputStream = new FileInputStream(filename)

    val hdr_len: Int = elfHeader.sizeBound.exact.get.toInt / 8
    val hdr_raw = elf.readNBytes(hdr_len)
    elf.skip(-hdr_len)
    val hdr: ElfHeader = elfHeader.decode(ByteVector.view(hdr_raw).toBitVector).require.value

    assert(hdr.e_ident_mag == ELF_HEADER_MAG)
    assert(hdr.e_machine == ELF_HEADER_MACHINE_RISCV)
    assert(hdr.e_ident_class == ELF_HEADER_IDENT_CLASS_32B)

    val ph_entries: Seq[PhEntry] = _get_ph_hdrs(elf, hdr)

    for (ph_entry <- ph_entries) {
      if (ph_entry.p_type == PT_LOAD) {
        elf.skip(ph_entry.p_offset.toInt)
        val mem: Array[Byte] = elf.readNBytes(ph_entry.p_filesz.toInt)
        elf.skip(-ph_entry.p_offset.toInt) // no `lseek(SEEK_SET)`

        val mem_expanded =
          mem ++ Array.fill[Byte](ph_entry.p_memsz.toInt - ph_entry.p_filesz.toInt)(0.toByte)
        mem_secs += MemSection(ph_entry, mem_expanded)
      }
    }

    mem_secs.toList
  }
}
